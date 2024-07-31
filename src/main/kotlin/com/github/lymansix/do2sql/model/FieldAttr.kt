package com.github.lymansix.do2sql.model

/**
 * @Description:
 * @Author: hutianhua
 * @CreateTime: 2023年08月21日
 */
class FieldAttr {


    /**
     * 字段名称
     */
    var name: String? = null

    /**
     * 字段类型
     */
    var javaType: String? = null

    /**
     * java全限定包名
     */
    var javaPackage: String? = null

    /**
     * sql 类型
     */
    var sqlType: String? = null

    /**
     * 字段说明
     */
    var comment: String = ""



    /**
     * 是否主键
     */
    var majorKey: Boolean = false


    /**
     * 是否自增
     */
    var autoIncrement: Boolean = false


    /**
     * 是否默认为null
     */
    var notNull: Boolean = false







}
