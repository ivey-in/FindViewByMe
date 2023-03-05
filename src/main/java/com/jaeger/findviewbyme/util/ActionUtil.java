package com.jaeger.findviewbyme.util;

import com.jaeger.findviewbyme.model.ViewPart;
import org.xml.sax.SAXException;

import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jaeger
 * 16/5/28.
 */
public class ActionUtil {

    public static List<ViewPart> getViewPartList(ViewSaxHandler viewSaxHandler, String oriContact) {
        try {
            if (oriContact != null) {
                viewSaxHandler.createViewList(oriContact);
                return viewSaxHandler.getViewPartList();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<ViewPart>();
    }
}
