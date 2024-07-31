package com.github.lymansix.do2sql.handle

import com.github.lymansix.do2sql.model.FieldAttr
import com.github.lymansix.do2sql.model.TableNameAttr
import kotlin.math.ceil

class CreateTableDDL {

    /**
     * 生成建表ddl语句
     */
    fun produce(fieldList: List<FieldAttr>, tableAttr: TableNameAttr): String {
        // 最大长度
        val len = fieldList.stream().mapToInt { it.name!!.length }.max().asInt
        // 最大制表符数量
        val pos = ((len / 4) + 1) * 4
        var sql = "\ndrop table if exists ${tableAttr.name};\n"
        sql += "create table `${tableAttr.name}` ( \n\t"
        var mainKey = ""
        var incStr = ""
        val columnsList = ArrayList<String>()
        for (fieldAttr in fieldList) {
            if (fieldAttr.majorKey) {
                mainKey = ",\n\tprimary key (`${fieldAttr.name}`)"
                if (fieldAttr.autoIncrement) {
                    incStr = " auto_increment=1"
                    var col = genColSql(fieldAttr, pos)
                    col += "not null auto_increment\tcomment '${fieldAttr.comment}'"
                    columnsList.add(col)
                } else {
                    var col = genColSql(fieldAttr, pos)
                    col += "not null\t\t\t\tcomment '${fieldAttr.comment}'"
                    columnsList.add(col)
                }
            } else {
                var col = genColSql(fieldAttr, pos)
                col += "default null\t\t\tcomment '${fieldAttr.comment}'"
                columnsList.add(col)
            }
        }
        sql = sql + columnsList.joinToString(",\n\t") + mainKey
        sql += "\n) engine=InnoDB${incStr} default charset=utf8mb4 collate=utf8mb4_unicode_ci row_format=dynamic comment='${tableAttr.comment}';"
        return sql + "\n"
    }

    private fun genColSql(fieldAttr: FieldAttr, pos: Int): String {
        var col = "`${fieldAttr.name}`"
        val l = ceil((pos - col.length) / 4.0).toInt()
        for (i in 0..l) {
            col += "\t"
        }
        col += "${fieldAttr.sqlType}"
        val l1 = ceil((16 - fieldAttr.sqlType!!.length) / 4.0).toInt()
        for (i in 0..l1) {
            col += "\t"
        }
        return col
    }


}
