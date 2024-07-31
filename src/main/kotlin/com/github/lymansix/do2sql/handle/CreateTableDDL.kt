package com.github.lymansix.do2sql.handle

import com.github.lymansix.do2sql.model.FieldAttr
import com.github.lymansix.do2sql.model.TableNameAttr

class CreateTableDDL {


    /**
     * 生成建表ddl语句
     */
    fun produce(fieldList: List<FieldAttr>, tableAttr: TableNameAttr): String {
        // 最大长度
        val len = fieldList.stream().mapToInt { it.name!!.length }.max().asInt
        // 最大制表符数量
        var pos = ((len / 4) + 1) * 4
        var sql: String = "\ndrop table if exists ${tableAttr.name};\n"
        sql = sql + "create table `${tableAttr.name}` ( \n\t"
        var mainKey: String = ""
        var incStr: String = ""
        val columnsList = ArrayList<String>()
        for (fieldAttr in fieldList) {
            if (fieldAttr.majorKey == true) {
                mainKey = ",\n\tprimary key (`${fieldAttr.name}`)"
                if (fieldAttr.autoIncrement == true) {
                    incStr = " auto_increment=1"
                    var col = "`${fieldAttr.name}`"
                    var l = Math.ceil((pos - col.length) / 4.0).toInt()
                    for (i in 0..l) {
                        col = col + "\t"
                    }
                    col += "${fieldAttr.sqlType}"
                    var l1 = Math.ceil((16 - fieldAttr.sqlType!!.length) / 4.0).toInt()
                    for (i in 0..l1) {
                        col += "\t"
                    }
                    col += "not null auto_increment\tcomment '${fieldAttr.comment}'"
                    columnsList.add(col)
                } else {
                    var col = "`${fieldAttr.name}`"
                    var l = Math.ceil((pos - col.length) / 4.0).toInt()
                    for (i in 0..l) {
                        col = col + "\t"
                    }
                    col += "${fieldAttr.sqlType}"
                    var l1 = Math.ceil((16 - fieldAttr.sqlType!!.length) / 4.0).toInt()
                    for (i in 0..l1) {
                        col += "\t"
                    }
                    col += "not null\t\t\t\tcomment '${fieldAttr.comment}'"
                    columnsList.add(col)
                }
            } else {
                var col = "`${fieldAttr.name}`"
                var l = Math.ceil((pos - col.length) / 4.0).toInt()
                for (i in 0..l) {
                    col = col + "\t"
                }
                col += "${fieldAttr.sqlType}"
                var l1 = Math.ceil((16 - fieldAttr.sqlType!!.length) / 4.0).toInt()
                for (i in 0..l1) {
                    col += "\t"
                }
                col += "default null\t\t\tcomment '${fieldAttr.comment}'"
                columnsList.add(col)
            }
        }
        sql = sql + columnsList.joinToString(",\n\t") + mainKey
        sql += "\n) engine=InnoDB${incStr} default charset=utf8mb4 collate=utf8mb4_unicode_ci row_format=dynamic comment='${tableAttr.comment}';"
        return sql + "\n";
    }


}
