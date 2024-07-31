package com.github.lymansix.do2sql.model

/**
 * @Description:
 * @Author: hutianhua
 * @CreateTime: 2023年08月24日
 */
class TableNameAttr{



    var name: String? = null


    var comment: String? = null

    /**
     * 索引类型
     */
    var engine: String = "InnoDB"

    /**
     * 字符编码
     */
    var charset: String = "CHARSET=utf8mb4"


    /**
     * 校验规则
     * COLLATE即使校验规则， 会影响到 ORDER BY 语句的顺序，
     * 会影响到 WHERE 条件中大于小于号筛选出来的结果，
     * 会影响DISTINCT、GROUP BY、HAVING语句的查询结果。
     * 另外，mysql 建索引的时候，如果索引列是字符类型，
     * 也会影响索引创建，只不过这种影响我们感知不到。
     * 总之，凡是涉及到字符类型比较或排序的地方，都会和 COLLATE 有关
     */
    var collate: String = "utf8mb4_unicode_ci"


    /**
     *  行格式
     */
    var rowFormat: String = "DYNAMIC"











}
