package com.jaeger.findviewbyme.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SyntheticElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jaeger.findviewbyme.model.ViewPart;
import com.jaeger.findviewbyme.util.ActionUtil;
import com.jaeger.findviewbyme.util.Utils;
import com.jaeger.findviewbyme.util.ViewSaxHandler;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

/**
 * Created by Jaeger
 * 15/11/25
 */
public class FindViewByMeAction extends AnAction {

    /**
     * 启动时触发
     */
    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        PsiFile psiFile = event.getData(LangDataKeys.PSI_FILE);
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            return;
        }

        List<ViewPart> viewParts;
        if ("XML".equalsIgnoreCase(psiFile.getFileType().getName())) {
            viewParts = getViewPartsXml(project, psiFile, editor);
        } else {
            viewParts = getViewParts(project, psiFile, editor);
        }

        FindViewDialog findViewDialog = new FindViewDialog();
        findViewDialog.setViewParts(project, editor, psiFile, getTargetClass(editor, psiFile), viewParts);
        findViewDialog.pack();
        findViewDialog.setLocationRelativeTo(WindowManager.getInstance().getFrame(project));
        findViewDialog.setVisible(true);
    }

    private List<ViewPart> getViewPartsXml(Project project, PsiFile psiFile, Editor editor) {
        ViewSaxHandler viewSaxHandler = new ViewSaxHandler();
        String contentStr = psiFile.getText();
        if (psiFile.getParent() != null) {
            viewSaxHandler.setLayoutPath(psiFile.getContainingDirectory().toString().replace("PsiDirectory:", ""));
            viewSaxHandler.setProject(project);
        }
        return ActionUtil.getViewPartList(viewSaxHandler, contentStr);
    }

    private List<ViewPart> getViewParts(Project project, PsiFile psiFile, Editor editor) {
        ViewSaxHandler viewSaxHandler = new ViewSaxHandler();

        PsiFile layout = Utils.getLayoutFileFromCaret(editor, psiFile);

        String contentStr = psiFile.getText();
        if (layout != null) {
            contentStr = layout.getText();
        }
        if (psiFile.getParent() != null) {
            String layoutPath = "";

            String javaPath = psiFile.getContainingDirectory().toString().replace("PsiDirectory:", "");

            String javaPathKey = "src" + File.separator + "main" + File.separator + "java";
            int indexOf = javaPath.indexOf(javaPathKey);
            if (indexOf != -1) {
                layoutPath = javaPath.substring(0, indexOf) + "src" + File.separator + "main" + File.separator + "res" + File.separator + "layout";
            }

            if (layoutPath.isEmpty()) {
                // 兼容自定义的 sourceSets
                javaPathKey = File.separator + "src" + File.separator;
                indexOf = javaPath.indexOf(javaPathKey);
                if (indexOf != -1) {
                    layoutPath = javaPath.substring(0, indexOf) + File.separator + "res" + File.separator + "layout";
                }
            }

            viewSaxHandler.setLayoutPath(layoutPath);
            viewSaxHandler.setProject(project);
        }
        return ActionUtil.getViewPartList(viewSaxHandler, contentStr);
    }

    @Nullable
    protected PsiClass getTargetClass(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        if (element == null) return null;
        final PsiClass target = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        return target instanceof SyntheticElement ? null : target;
    }

}
