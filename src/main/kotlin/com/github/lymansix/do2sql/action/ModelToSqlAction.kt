package com.github.lymansix.do2sql.action

import com.github.lymansix.do2sql.handle.CreateTableDDL
import com.github.lymansix.do2sql.handle.DOFiledHandle
import com.github.lymansix.do2sql.model.TableNameAttr
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.SyntheticElement
import com.intellij.psi.util.PsiTreeUtil


class ModelToSqlAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val psiFile = event.getData(CommonDataKeys.PSI_FILE)
        val editor = event.getData(CommonDataKeys.EDITOR)
        if (editor == null || project == null || psiFile == null || psiFile.isDirectory) {
            return
        }
        val psiClazz = this.getTargetClass(editor, psiFile)
        if (psiClazz != null) {
            // 限制类型
            if (psiClazz.isInterface || psiClazz.isEnum) {
                return
            }
            val handle = DOFiledHandle()
            // 获取表字段
            var fieldList = handle.handel(psiClazz, project)
            fieldList = fieldList.filter { it.sqlType != null }
            // 获取表属性
            val tableAttr = handle.handleTable(psiClazz, psiFile)
            // 生成sql
            val sql = CreateTableDDL().produce(fieldList, tableAttr)
            // 打开工具面板
            this.showToolWindow(sql, project, tableAttr)
        }
    }


    /**
     * 显示窗口
     */
    private fun showToolWindow(sql: String, project: Project, tableAttr: TableNameAttr) {

        // 工具窗口
        val toolWindowManager = ToolWindowManager.getInstance(project)
        var toolWindow = toolWindowManager.getToolWindow(ToolWindowId.RUN)
        if (toolWindow == null) {
            // 注册窗口
            toolWindow = toolWindowManager.registerToolWindow(ToolWindowId.RUN) {
                canCloseContent = true
            }
        }
        // 窗口管理器
        val contentManager = toolWindow.contentManager
        // tab 窗口标题
        val displayName = "表[${tableAttr.name}]"
        // 清除和当前tab相同的窗口
        for (content in contentManager.contents) {
            if (content.displayName == displayName) {
                contentManager.removeContent(content, true)
            }
        }
        // 控制台view
        val consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
        val content = contentManager.factory.createContent(consoleView.component, displayName, true)
        contentManager.addContent(content)
        consoleView.clear()
        consoleView.print(sql, ConsoleViewContentType.NORMAL_OUTPUT)

        toolWindow.show()
        if (toolWindow.isVisible) {
            contentManager.setSelectedContent(content, true)
        }
    }


    /**
     * 只有是java文件才能显示处理
     * @param event
     */
    override fun update(event: AnActionEvent) {
        try {
            val virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE)
            val psiFile = event.getData(CommonDataKeys.PSI_FILE)
            val editor = event.getData(CommonDataKeys.EDITOR)
            if (psiFile == null || editor == null) {
                event.presentation.isVisible = false
            } else {
                val psiClazz = this.getTargetClass(editor, psiFile)!!
                val enable = "JAVA" == virtualFile!!.fileType.name
                event.presentation.isVisible = !psiClazz.isInterface && !psiClazz.isEnum && enable
            }
        } catch (e: Exception) {
            event.presentation.isVisible = false
        }
    }


    /**
     * 获取目标对象
     * @param editor
     * @param file
     * @return
     */
    private fun getTargetClass(editor: Editor, file: PsiFile): PsiClass? {
        val offset = editor.caretModel.offset
        val element = file.findElementAt(offset)
        if (element != null) {
            val target = PsiTreeUtil.getParentOfType(element, PsiClass::class.java)
            return if (target is SyntheticElement) null else target
        }
        return null
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}
