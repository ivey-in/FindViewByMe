package com.jaeger.findviewbyme.util;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.jaeger.findviewbyme.model.FindViewPropes;
import com.jaeger.findviewbyme.model.ViewPart;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengwei on 16/5/20.
 */
public class JavaCodeWriter extends WriteCommandAction.Simple {

    private Editor mEditor;
    private PsiFile psiFile;
    protected PsiClass mClass;
    protected Project mProject;
    protected PsiElementFactory mFactory;

    private List<ViewPart> viewPartList = new ArrayList<>();
    private List<String> template = new ArrayList<>();

    public JavaCodeWriter(Editor editor, PsiFile psiFile, PsiClass clazz, List<ViewPart> viewPartList, List<String> template) {
        super(clazz.getProject(), "");
        mEditor = editor;
        this.psiFile = psiFile;
        mClass = clazz;
        mProject = clazz.getProject();
        mFactory = JavaPsiFacade.getElementFactory(mProject);

        if (viewPartList != null) {
            this.viewPartList.clear();
            this.viewPartList.addAll(viewPartList);
        }

        if (template != null) {
            this.template.clear();
            this.template.addAll(template);
        }
    }

    /**
     * judge field exists
     *
     * @param part
     * @return
     */
    private boolean fieldExist(ViewPart part) {
        PsiField[] fields = mClass.getAllFields();
        for (PsiField field : fields) {
            if (field.getName().equals(part.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * get initView method
     *
     * @return
     */
    private PsiMethod getFindViews() {
        PsiMethod[] methods = mClass.findMethodsByName("findViews", true);
        for (PsiMethod method : methods) {
            if (method.getReturnType().equals(PsiType.VOID)) {
                return method;
            }
        }

        return null;
    }

    @Override
    protected void run() throws Throwable {

        List<ViewPart> viewPartsToAdd = new ArrayList<>();
        for (ViewPart viewPart : viewPartList) {
            if (viewPart.isSelected() && !fieldExist(viewPart)) {
                viewPartsToAdd.add(viewPart);
            }
        }

        if (viewPartsToAdd.isEmpty()) return;

        PsiMethod findViewsMethod = getFindViews();
        StringBuilder methodBuilder = new StringBuilder();
        if (findViewsMethod == null) {
            methodBuilder.append("private void findViews() {");
        }
        for (int i = 0; i < template.size(); i++) {
            final String line = template.get(i);
            for (ViewPart viewPart : viewPartsToAdd) {
                String text = line.replaceAll(FindViewPropes._TYPE, viewPart.getType())
                        .replaceAll(FindViewPropes._NAME, viewPart.getName())
                        .replaceAll(FindViewPropes._ID, "R.id." + viewPart.getId());
                if (i == 0) {
                    // 声明语句
                    mClass.add(mFactory.createFieldFromText(text, mClass));
                } else {
                    if (findViewsMethod != null) {
                        findViewsMethod.getBody().add(mFactory.createStatementFromText(text, mClass));
                    } else {
                        methodBuilder.append(text);
                    }
                }
            }
        }
        if (findViewsMethod == null) {
            methodBuilder.append("}");
            PsiMethod method = mFactory.createMethodFromText(methodBuilder.toString(), mClass);
            mClass.add(method);
        }

        // reformat class
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mProject);
        styleManager.optimizeImports(psiFile);
        styleManager.shortenClassReferences(mClass);
        new ReformatCodeProcessor(mProject, mClass.getContainingFile(), null, false).runWithoutProgress();
    }
}
