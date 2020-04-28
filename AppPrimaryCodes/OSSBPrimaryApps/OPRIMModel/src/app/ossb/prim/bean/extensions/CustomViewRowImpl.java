package app.ossb.prim.bean.extensions;

import oracle.jbo.LocaleContext;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.Entity;
import oracle.jbo.server.ViewRowAttrHintsImpl;
import oracle.jbo.server.ViewRowImpl;

/**
 * Custom ADF Framework Extension Class for View Object Rows
 * @author Hadi Wijaya
 * @version 1.0
 */
public class CustomViewRowImpl extends ViewRowImpl {
//  @Override
//  protected ViewRowAttrHintsImpl createViewRowAttrHints(AttributeDefImpl attrDef) {
//      return new CustomViewRowAttrHints(attrDef,this);
//  }
//  class CustomViewRowAttrHints extends ViewRowAttrHintsImpl {
//      protected CustomViewRowAttrHints(AttributeDefImpl attr, ViewRowImpl viewRow) {
//         super(attr,viewRow);
//      }
//      @Override
//      public String getHint(LocaleContext locale, String sHintName) {
//          if ("rowState".equals(sHintName)) {
//            ViewRowImpl vri = getViewRow();
//            if (vri != null) {
//                Entity e = vri.getEntity(0);
//                if (e != null) {
//                    return translateStatusToString(e.getEntityState());
//                }
//                return null;
//            }
//          }
//          else if ("valueChanged".equals(sHintName)) {
//              ViewRowImpl vri = getViewRow();
//              if (vri != null) {
//                  boolean changed = vri.isAttributeChanged(getViewAttributeDef().getName());
//                  return changed ? "true":"false";
//              }
//          }
//          return super.getHint(locale, sHintName);
//      }
//      private String translateStatusToString(byte b) {
//        String ret = null;
//        switch (b) {
//          case Entity.STATUS_DELETED: {
//            ret = "Deleted";
//            break;
//          }
//          case Entity.STATUS_INITIALIZED: {
//            ret = "Initialized";
//            break;
//          }
//          case Entity.STATUS_MODIFIED: {
//            ret = "Modified";
//            break;
//          }
//          case Entity.STATUS_UNMODIFIED: {
//            ret = "Unmodified";
//            break;
//          }
//          case Entity.STATUS_NEW: {
//            ret = "New";
//            break;
//          }
//        }
//        return ret;
//      }        
//  }

}
