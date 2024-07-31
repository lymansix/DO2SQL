package com.github.lymansix.do2sql.handle
import com.github.lymansix.do2sql.model.FieldAttr
import com.github.lymansix.do2sql.model.TableNameAttr
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import java.util.*

/**
 * @Description:
 * @Author: hutianhua
 * @CreateTime: 2023年08月21日
 */
class DOFiledHandle {


    private val map = mutableMapOf(
        "String" to "varchar(255)",
        "int" to "int",
        "Integer" to "int",
        "long" to "bigint",
        "Long" to "bigint",
        "double" to "double",
        "Double" to "double",
        "Boolean" to "tinyint(1)",
        "Date" to "datetime",
        "Timestamp" to "timestamp",
        "Time" to "TIME",
        "BigDecimal" to "decimal(18, 4)",
        "LocalDateTime" to "datetime",
        "LocalDate" to "date",
    )


    fun handel(clazz: PsiClass?, project: Project): List<FieldAttr> {

        val resList = arrayListOf<FieldAttr>()
        if (clazz != null && !clazz.isInterface && !clazz.isEnum) {

            val clazzAll = this.findAllClass(clazz)
            clazzAll.forEach { cl ->
                val allFields = cl.fields
                for (fields in allFields) {
                    val fieldAttr = FieldAttr()
                    // 如果是静态变量跳过
                    if (fields.hasModifierProperty(PsiModifier.STATIC)) {
                        continue
                    }

                    // 字段名字
                    fieldAttr.name = this.camelToUnderscore(fields.name)
                    if ("serialVersionUID" == fields.name) {
                        continue
                    }
                    // java 字段类型
                    fieldAttr.javaType = fields.type.presentableText
                    fieldAttr.javaPackage = fields.type.canonicalText
                    var boo = false
                    for (annotation in fields.annotations) {
                        // 获取注解的全限定名称
                        val name = annotation.qualifiedName!!
                        // 有id注解 判断为主键
                        if (name.endsWith(".Id")) {
                            fieldAttr.majorKey = true
                            fieldAttr.notNull = true
                        }

                        // 有TableId注解的判断为主键,主键不能为空
                        if (name.endsWith(".TableId")) {
                            fieldAttr.majorKey = true
                            fieldAttr.notNull = true
                            annotation.findAttributeValue("type")?.also {
                                it.text.contains(".AUTO").also {
                                    fieldAttr.autoIncrement = true
                                }
                            }
                        }

                        // 获取 swagger 中 ApiModelProperty 的属性值
                        if (name.endsWith(".ApiModelProperty")) {
                            annotation.findAttributeValue("value")?.let {
                                fieldAttr.comment = it.text.replace("\\", "")
                            }
                        }

                        // 解析 swagger 中 TableField 注解的 属性
                        if (name.endsWith(".TableField")) {
                            annotation.findAttributeValue("value")?.text?.let {
                                val str = it.replace("\"", "").replace("`", "")
                                if (str != "") {
                                    fieldAttr.name = this.camelToUnderscore(str)
                                }
                            }
                            val exist = annotation.findAttributeValue("exist")
                            boo = exist?.textMatches("false") ?: false
                        }
                    }


                    if (boo) continue

                    // 没有 ApiModelProperty 注解，则找注释
                    if (fieldAttr.comment == "") {
                        // 获取注释
                        val docComment = fields.docComment
                        if (docComment != null) {
                            // 获取javadoc 注释文档上的注释
                            val docList = ArrayList<String>()
                            docComment.descriptionElements.forEach { dolces ->
                                dolces.text?.trim()?.let {
                                    if (it != "") {
                                        docList.add(it.replace("\\", ""))
                                    }
                                }
                            }
                            fieldAttr.comment = docList.joinToString(";")
                            // 获取双斜杠的注释
                            if (fieldAttr.comment == "") {
                                val owner = docComment.owner
                                if (owner != null) {
                                    fieldAttr.comment = this.getColumnExplain(owner.children)
                                }
                            }
                        } else {
                            // 获取双斜线注释
                            fieldAttr.comment = this.getColumnExplain(fields.children)
                        }
                    }
                    this.parseFiledType(fieldAttr, project, fields)
                    resList.add(fieldAttr)
                }

            }


        }
        return resList
    }


    /**
     * 所有类，降序
     */
    private fun findAllClass(clazz: PsiClass): LinkedList<PsiClass> {
        val linkedList = LinkedList<PsiClass>()
        linkedList.addFirst(clazz)
        var superClass = clazz.superClass
        while (superClass != null && "java.lang.Object" != superClass.qualifiedName) {
            linkedList.addFirst(superClass)
            superClass = superClass.superClass
        }
        return linkedList
    }


    /**
     * 处理字段类型
     */
    private fun parseFiledType(fieldAttr: FieldAttr, project: Project, field: PsiField) {
        val str = "com.baomidou.mybatisplus.annotation.EnumValue"
        val javaType = fieldAttr.javaType
        val javaPackage = fieldAttr.javaPackage
        val sqlType = map[javaType]
        if (sqlType != null) {
            fieldAttr.sqlType = sqlType
        } else if (javaPackage != null) {
            val annotation = field.getAnnotation(str)
            if (annotation != null) {
                fieldAttr.sqlType = "varchar(255)"
            } else {
                val psiClass =
                    JavaPsiFacade.getInstance(project).findClass(javaPackage, GlobalSearchScope.allScope(project))
                psiClass?.also {
                    if (it.isEnum) {
                        fieldAttr.sqlType = "varchar(255)"
                        for (filed in it.allFields) {
                            filed.getAnnotation(str)?.let {
                                val presentableText = filed.type.presentableText
                                fieldAttr.sqlType = map[presentableText]
                                return
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * 获取表注释
     */
    private fun getColumnExplain(list: Array<PsiElement>): String {
        val doc = StringJoiner(";")
        for (child in list) {
            if ("END_OF_LINE_COMMENT" == child.node?.elementType?.toString()) {
                if (child.text.trim().isNotEmpty()) {
                    doc.add(child.text.trim().replace("//", "").trim())
                }
            }
        }
        return doc.toString()
    }


    /**
     * 处理表名称
     */
    fun handleTable(clazz: PsiClass, psiFile: PsiFile): TableNameAttr {
        val tableNameAttr = TableNameAttr()

        // 先获取注解上表明
        val tableName = clazz.getAnnotation("com.baomidou.mybatisplus.annotation.TableName")
        if (tableName != null) {
            val findAttributeValue = tableName.findAttributeValue("value")
            if (findAttributeValue != null) {
                tableNameAttr.name = findAttributeValue.text.trim().replace("\"", "").trim()
            }
        }
        // 没有注解 或者注解没有设置 使用表明
        if (tableNameAttr.name == null) {
            val name = clazz.name!!
            tableNameAttr.name = this.camelToUnderscore(name)
        }
        // 获取表的注释
        val apiModel = clazz.getAnnotation("io.swagger.annotations.ApiModel")
        if (apiModel != null) {
            val findAttributeValue = apiModel.findAttributeValue("value")
            if (findAttributeValue != null) {
                tableNameAttr.comment = findAttributeValue.text.trim().replace("\"", "").trim()
            }
        }
        // 没有使用swagger 获取注释
        if (tableNameAttr.comment == null) {
            val docComment = clazz.docComment
            if (docComment != null) {
                // 获取javadoc 注释文档上的注释
                val docList = ArrayList<String>()
                for (dowels in docComment.descriptionElements) {
                    if (dowels.text?.trim() != null && dowels.text.trim().isNotEmpty()) {
                        docList.add(dowels.text.replace("\"", ""))
                    }
                }
                tableNameAttr.comment = docList.joinToString(";")
                // 获取双斜杠的注释
                val owner = docComment.owner
                if (owner != null) {
                    tableNameAttr.comment = this.getColumnExplain(owner.children)
                }
            } else {
                // 获取双斜线注释
                tableNameAttr.comment = this.getColumnExplain(psiFile.children)

            }
        }


        return tableNameAttr
    }


    /**
     * 驼峰转下滑线
     */
    private fun camelToUnderscore(param: String): String {
        val str = param.trim()
        if (str.isEmpty()) return ""
        val list = mutableListOf<String>()
        var i = 1
        var j = 0
        while (i < str.length) {
            if (str[i] in 'A'..'Z') {
                list.add(str.substring(j, i))
                j = i
            }
            i++
        }
        list.add(str.substring(j))
        return list.joinToString("_") { it.lowercase(Locale.getDefault()) }
    }


}
