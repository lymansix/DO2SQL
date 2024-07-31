package com.github.lymansix.do2sql.handle
/**
 * @Description: 常量
 * @Author: hutianhua
 * @CreateTime: 2023年09月07日
 */
interface SqlConst {
    companion object {
        /**
         * 主键
         */
        val PRIMARY_KEY: String = "PRIMARY KEY"

        /**
         * 主键自增
         */
        val AUTO_INCREMENT: String = "AUTO_INCREMENT"

        /**
         * 不为空
         */
        val DEFAULT_NULL: String = "DEFAULT NULL"

        /**
         * 为空
         */
        val NULL: String = "NULL"

    }
}
