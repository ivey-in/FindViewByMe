package com.jaeger.findviewbyme.model;

import com.intellij.ide.util.PropertiesComponent;
import com.jaeger.findviewbyme.util.TextUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FindViewPropes {
    public static final String PROPERTY_CUR_TEMP = "findviewbyme_cur_temp";
    public static final String PROPERTY_ALL_TEMP = "findviewbyme_all_temp";
    public static final String PROPERTY_VAR_PREFIX_ENABLED = "findviewbyme_var_prefix_enabled";
    public static final String PROPERTY_VAR_PREFIX = "findviewbyme_var_prefix";

    public static final String _NAME = "@N";
    public static final String _TYPE = "@T";
    public static final String _ID = "@I";
    public static final String _LINE = "-->";
    public static final String DEFAULT_TEMP = "private val @N by lazyFind<@T>(@I)\n"
            + "val @N = itemView.findViewById<@T>(@I)\n"
            + "private @T @N;-->@N = findViewById(@I);";

    public static final String TIPS = "@N for name, @T for type, @I for id, --> for statement-separator";

    private String curTemp;
    private String allTemp;

    private boolean varPrefixEnabled;
    private String varPrefix;

    public static FindViewPropes getInstance() {
        FindViewPropes data = new FindViewPropes();
        data.curTemp = PropertiesComponent.getInstance().getValue(PROPERTY_CUR_TEMP);
        data.allTemp = PropertiesComponent.getInstance().getValue(PROPERTY_ALL_TEMP);
        data.varPrefixEnabled = PropertiesComponent.getInstance().getBoolean(PROPERTY_VAR_PREFIX_ENABLED);
        data.varPrefix = PropertiesComponent.getInstance().getValue(PROPERTY_VAR_PREFIX, "m");
        return data;
    }

    public void save() {
        PropertiesComponent.getInstance().setValue(PROPERTY_CUR_TEMP, curTemp);
        PropertiesComponent.getInstance().setValue(PROPERTY_ALL_TEMP, allTemp);
        PropertiesComponent.getInstance().setValue(PROPERTY_VAR_PREFIX_ENABLED, varPrefixEnabled);
        PropertiesComponent.getInstance().setValue(PROPERTY_VAR_PREFIX, varPrefix);
    }

    public String getCurTemp() {
        return curTemp;
    }

    @Nonnull
    public List<String> getCurTempList() {
        String temp = curTemp;
        if (TextUtils.isEmpty(temp)) {
            List<String> allTemp = getAllTempList();
            if (!allTemp.isEmpty()) {
                temp = allTemp.get(0);
            }
        }
        if (!TextUtils.isEmpty(temp)) {
            return Arrays.asList(temp.split(_LINE));
        } else {
            return new ArrayList<>();
        }
    }

    public boolean setCurTemp(String curTemp) {
        curTemp = TextUtils.trimLines(curTemp);
        if (!TextUtils.equals(this.curTemp, curTemp)) {
            this.curTemp = curTemp;
            PropertiesComponent.getInstance().setValue(PROPERTY_CUR_TEMP, curTemp);
            return true;
        }
        return false;
    }

    public String getAllTemp() {
        return (allTemp != null && !allTemp.trim().isEmpty()) ? allTemp : DEFAULT_TEMP;
    }

    @Nonnull
    public List<String> getAllTempList() {
        return Arrays.asList(getAllTemp().split("\n"));
    }

    public boolean setAllTemp(String allTemp) {
        allTemp = TextUtils.trimLines(allTemp);

        if (!TextUtils.equals(this.allTemp, allTemp)) {
            this.allTemp = allTemp;
            PropertiesComponent.getInstance().setValue(PROPERTY_ALL_TEMP, allTemp);
            return true;
        }
        return false;
    }

    public boolean isVarPrefixEnabled() {
        return varPrefixEnabled;
    }

    public boolean setVarPrefixEnabled(boolean varPrefixEnabled) {
        if (this.varPrefixEnabled != varPrefixEnabled) {
            this.varPrefixEnabled = varPrefixEnabled;
            PropertiesComponent.getInstance().setValue(PROPERTY_VAR_PREFIX_ENABLED, varPrefixEnabled);
            return true;
        }
        return false;
    }

    public String getVarPrefix() {
        return varPrefix;
    }

    public boolean setVarPrefix(String varPrefix) {
        varPrefix = TextUtils.trimLines(varPrefix);

        if (!TextUtils.equals(this.varPrefix, varPrefix)) {
            this.varPrefix = varPrefix;
            PropertiesComponent.getInstance().setValue(PROPERTY_VAR_PREFIX, varPrefix);
            return true;
        }
        return false;
    }
}
