package com.jaeger.findviewbyme.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiFile;
import com.jaeger.findviewbyme.model.ViewPart;
import com.jaeger.findviewbyme.util.ActionUtil;
import com.jaeger.findviewbyme.util.ViewSaxHandler;

import java.util.List;

public class FindViewByMeSelectionAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        PsiFile psiFile = event.getData(LangDataKeys.PSI_FILE);
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            return;
        }

        List<ViewPart> viewParts = getViewParts(project, psiFile, editor);
        FindViewDialog findViewDialog = new FindViewDialog();
        findViewDialog.setViewParts(project, editor, psiFile, null, viewParts);
        findViewDialog.pack();
        findViewDialog.setLocationRelativeTo(WindowManager.getInstance().getFrame(project));
        findViewDialog.setVisible(true);
    }

    private List<ViewPart> getViewParts(Project project, PsiFile psiFile, Editor editor) {
        ViewSaxHandler viewSaxHandler = new ViewSaxHandler();

        String contentStr = editor.getSelectionModel().getSelectedText();
        if (contentStr != null) {
            // 保证是合法的 xml 格式，比如选中的是 <View /> <View />，包裹之后是 <t-t> <View /> <View /> </t-t>
            contentStr = "<t-t>" + contentStr + "</t-t>";
        }
        if (psiFile.getParent() != null) {
            viewSaxHandler.setLayoutPath(psiFile.getContainingDirectory().toString().replace("PsiDirectory:", ""));
            viewSaxHandler.setProject(project);
        }
        return ActionUtil.getViewPartList(viewSaxHandler, contentStr);
    }

}
