package k.ebean

/**
 * Created with IntelliJ IDEA.
 * User: kk
 * Date: 13-11-21
 * Time: 上午10:41
 * To change this template use File | Settings | File Templates.
 */

import com.avaje.ebean.Ebean
import com.avaje.ebean.EbeanServer
import com.avaje.ebean.TxIsolation
import com.avaje.ebean.TxScope
import com.google.common.reflect.ClassPath
import jodd.util.StringUtil
import k.aop.annotations.DBIndexed
import k.common.BizLogicException
import k.common.Hub
import javax.persistence.Entity
import javax.persistence.Table
import kotlin.reflect.memberProperties

internal class IndexInfo(var indexName: String) {

    var columns: MutableSet<String>

    init {
        this.columns = mutableSetOf()
    }

    fun AddClumn(columnName: String) {
        this.columns.add(columnName)
    }

    fun IsCombinedIndex(): Boolean {
        return this.columns.size > 1
    }

    companion object {

        fun LoadIndexInfoForTable(tableName: String): Map<String, IndexInfo> {
            val indexMap = mutableMapOf<String, IndexInfo>()
            val sql = String.format("show index from `%s`", tableName)
            val rows = DB.Default().createSqlQuery(sql).findList()
            for (row in rows) {
                val columnName = row.getString("Column_name")
                val indexName = row.getString("Key_name")

                if (!indexMap.containsKey(indexName)) {
                    indexMap.put(indexName, IndexInfo(indexName))
                }

                val indexInfo = indexMap.get(indexName)
                indexInfo!!.AddClumn(columnName)
            }

            return indexMap
        }

        // 判断指定的字段是否有索引, 排除联合索引
        fun IndexExists(indexMap: Map<String, IndexInfo>, columnName: String): Boolean {
            for ((indexName, indexInfo) in indexMap) {

                if (indexInfo.IsCombinedIndex()) continue

                if (indexInfo.columns.contains(columnName)) return true
            }
            return false
        }
    }

}

object DbIndex {

    fun GetCreateIndexSql(): String {
        val sb = StringBuilder()
        val cp = ClassPath.from(Hub.application().classloader())
        val classes = cp.getTopLevelClassesRecursive("models")
        for (classInfo in classes) {
            val modelClass = classInfo.load()
            if (isEntityClass(classInfo.load())) {
                val tableName = getTableName(modelClass)
                val dropSql = getDropIndexSqlBy(modelClass)
                val createSql = getCreateIndexSqlByModel(modelClass)
                if ((dropSql + createSql).isNotBlank()) {
                    sb.append("-- ").append("================================\n")
                    sb.append("-- ").append("Table: ").append(tableName).append("\n")
                    sb.append("-- ").append("================================\n")
                    sb.append(dropSql)
                    sb.append("\n")
                    sb.append(createSql)
                    sb.append("\n")
                }
            }
        }
        return sb.toString()
    }

    private fun isEntityClass(modelClass: Class<*>): Boolean {
        val annoEntity = modelClass.getAnnotation(Entity::class.java)
        return annoEntity != null
    }

    private fun getTableName(modelClass: Class<*>): String {
        modelClass.getAnnotation(Entity::class.java) ?:
                throw BizLogicException("不是实体类")

        val annoTable = modelClass.getAnnotation(Table::class.java)
        if (annoTable != null && annoTable.name.isNotBlank()) {
            return annoTable.name
        } else {
            return StringUtil.fromCamelCase(modelClass.getSimpleName(), '_')
        }

    }

    private fun getIndexedFieldNames(modelClass: Class<*>): Set<String> {
        return modelClass.kotlin.memberProperties
                .filter {  it.annotations.filter { it is DBIndexed }.size > 0 }
                .map { it.name }
                .toSet()
    }

    private fun getIndexedColumns(tableName: String): Map<String, String> {
        val sql = String.format("show index from `%s`", tableName)
        val rows = Ebean.createSqlQuery(sql).findList()

        return rows.map {
            Pair(it.getString("Column_name"), it.getString("Key_name"))
        }.toMap()
    }


    private fun getCreateIndexSqlByModel(modelClass: Class<*>): String {
        val tableName = getTableName(modelClass)
        val fieldNames = getIndexedFieldNames(modelClass)

        val sb = StringBuilder()

        val indexMap = IndexInfo.LoadIndexInfoForTable(tableName)
        for (fieldName in fieldNames) {
            if (!IndexInfo.IndexExists(indexMap, fieldName)) {
                // 对应的字段索引不存在
                val idx_name = String.format("idx_%s_%s", tableName, fieldName)
                val create_sql = String.format("CREATE INDEX `%s` ON `%s` (`%s`);",
                        idx_name,
                        tableName,
                        fieldName)
                sb.append(create_sql).append("\n")
            }
        }

        if (sb.length > 0) {
            sb.append("\n")
        }

        return sb.toString()
    }

    private fun getDropIndexSqlBy(modelClass: Class<*>): String {
        val tableName = getTableName(modelClass)
        val indexedFields = getIndexedFieldNames(modelClass)

        val sb = StringBuilder()

        val indexMap = IndexInfo.LoadIndexInfoForTable(tableName)
        for (indexInfo in indexMap.values) {
            if (indexInfo.IsCombinedIndex()) {
                continue
            }

            if (indexInfo.indexName.toUpperCase() == "PRIMARY") {
                continue
            }

            if (indexInfo.indexName.startsWith("uq_")) {
                continue
            }

            if (indexInfo.columns.filter { it in indexedFields }.size == 0) {
                sb.append(String.format("DROP INDEX `%s` ON `%s`;\n",
                        indexInfo.indexName,
                        tableName))
            }
        }

        if (sb.length > 0) {
            sb.append("\n")
        }
        return sb.toString()
    }
}

object DB {

    fun Default(): EbeanServer {
        return Ebean.getServer(null)
    }

//    fun <T> RunInTransaction(txCallable: TxCallable<T>): T {
//        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED)
//        return Ebean.execute(txScope, txCallable)
//    }
//
//    fun RunInTransaction(txRunnable: TxRunnable) {
//        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED)
//        Ebean.execute(txScope, txRunnable)
//    }
//
//    fun RunInTransaction(runnable: Runnable) {
//        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED)
//        Ebean.execute(txScope, { runnable })
//    }
//
//    fun <T> RunInTransaction(callable: Callable<T>) {
//        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED)
//        return Ebean.execute(txScope, { callable })
//    }

    fun RunInTransaction(body: () -> Unit) {
        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED)
        Ebean.execute(txScope, body)
    }

    fun <T> RunInTransaction(body: () -> T): T {
        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED)
        return Ebean.execute(txScope, body)
    }

    fun TableExists(tableName: String): Boolean {
        val rows = Default().createSqlQuery("SHOW TABLES").findList()
        val count = rows.count { it.values.first() == tableName }
        return count > 0
    }

}
