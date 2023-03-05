package com.jaeger.findviewbyme.action;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.jaeger.findviewbyme.model.FindViewPropes;
import com.jaeger.findviewbyme.model.ViewPart;
import com.jaeger.findviewbyme.util.JavaCodeWriter;
import com.jaeger.findviewbyme.util.TextUtils;
import com.jaeger.findviewbyme.util.Utils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class FindViewController {
    FindViewPropes propes = FindViewPropes.getInstance();

    private List<ViewPart> viewParts = new ArrayList<>();
    private String searchWord = null;
    private int firstSearchIndexMatched = -1;
    private int searchIndexMatched = -1;

    public boolean setVarPrefixEnabled(boolean varPrefixEnabled) {
        if (propes.setVarPrefixEnabled(varPrefixEnabled)) {
            updateName();
            return true;
        }
        return false;
    }

    public boolean setVarPrefix(String varPrefix) {
        if (propes.setVarPrefix(varPrefix)) {
            updateName();
            return true;
        }
        return false;
    }

    private void updateName() {
        String prefix = propes.isVarPrefixEnabled() ? propes.getVarPrefix() : null;
        for (ViewPart viewPart : viewParts) {
            if (!TextUtils.isEmpty(prefix)) {
                viewPart.addPrefixForName(prefix);
            } else {
                viewPart.resetName();
            }
        }
    }

    public List<ViewPart> getViewParts() {
        return viewParts;
    }

    private Editor editor;
    private PsiFile file;
    PsiClass psiClass;

    @Nonnull
    public void setViewParts(Editor editor, PsiFile file, PsiClass psiClass, List<ViewPart> viewParts) {
        this.editor = editor;
        this.file = file;
        this.psiClass = psiClass;
        this.viewParts.clear();
        if (viewParts != null) {
            this.viewParts.addAll(viewParts);
        }
        updateName();
        resetSearch();
    }

    private void resetSearch() {
        searchWord = null;
        searchIndexMatched = -1;
        firstSearchIndexMatched = -1;
    }

    public int search(String word) {
        ensureSearchMatched(word);
        if (searchIndexMatched < 0) {
            searchIndexMatched = firstSearchIndexMatched;
        } else {
            for (int i = (searchIndexMatched + 1); i < viewParts.size(); i++) {
                if (Utils.bruteFore(viewParts.get(i).getName(), word) != -1) {
                    // 匹配上了
                    searchIndexMatched = i;
                    return i;
                }
            }
            if (searchIndexMatched >= (viewParts.size() - 1)) {
                searchIndexMatched = firstSearchIndexMatched;
            }
        }
        return searchIndexMatched;
    }

    private void ensureSearchMatched(String word) {
        if (!TextUtils.equals(searchWord, word)) {
            searchWord = word;
            searchIndexMatched = -1;
            firstSearchIndexMatched = -1;

            if (!TextUtils.isEmpty(word)) {
                for (int i = 0; i < viewParts.size(); i++) {
                    if (Utils.bruteFore(viewParts.get(i).getName(), word) != -1) {
                        // 匹配上了
                        firstSearchIndexMatched = i;
                        return;
                    }
                }
            }
        }
    }

    public void selectNone() {
        for (ViewPart viewPart : viewParts) {
            viewPart.setSelected(true);
        }
    }

    public void selectAll() {
        for (ViewPart viewPart : viewParts) {
            viewPart.setSelected(false);
        }
    }

    public void negativeSelect() {
        for (ViewPart viewPart : viewParts) {
            viewPart.setSelected(!viewPart.isSelected());
        }
    }

    public void select(int index, boolean selected) {
        if (index >= 0 && index < viewParts.size()) {
            viewParts.get(index).setSelected(selected);
        }
    }

    public boolean isInjectCodeSupported() {
        return psiClass != null;
    }

    public void injectCode() {
        if (isInjectCodeSupported()) {
            new JavaCodeWriter(editor, file, psiClass, viewParts, propes.getCurTempList()).execute();
        }
    }

    public String generateCode() {
        StringBuilder sb = new StringBuilder();
        List<String> template = propes.getCurTempList();
        for (int i = 0; i < template.size(); i++) {
            if (i != 0) {
                sb.append("\n");
            }
            String line = template.get(i);
            for (ViewPart viewPart : viewParts) {
                sb.append(line.replaceAll(FindViewPropes._TYPE, viewPart.getType())
                                .replaceAll(FindViewPropes._NAME, viewPart.getName())
                                .replaceAll(FindViewPropes._ID, "R.id." + viewPart.getId()))
                        .append("\n");
            }
        }
        return sb.toString();
    }
}
