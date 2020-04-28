package app.ossb.prim.bean.extensions;

import java.util.Hashtable;
import oracle.jbo.AttributeDef;
import oracle.jbo.server.AssociationDefImpl;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.Entity;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;
import oracle.jbo.server.TransactionEvent;

public class CustomEntityImpl extends EntityImpl {
    private static final String POST_AFTER_ACCESSORS = "PostAfterAccessors";
    private static final String REGEX_COMMA_WITH_OPT_WHITESPACE = ",\\s*";
    /*
     * Overridden framework method
     *
     * This code overrides the default processing
     */

    @Override
    public void postChanges(TransactionEvent e) {
        String[] postAfterAccessors = getPostAfterAccessors();
        /* Only perform this processing if postAfterAccessors prop != null */
        if (postAfterAccessors != null) {
            byte postState = getPostState();
            /* If current entity is new or modified */
            if (postState == STATUS_NEW || postState == STATUS_MODIFIED) {
                for (String postAfterAccessor : postAfterAccessors) {
                    /*
                     * Only check the referenced entity if it is not a
                     * composition and it at least one of the association
                     * attributes has changed (to avoid retrieving
                     * referenced entity into cache unnecessarily.
                     */
                    if (!isComposition(postAfterAccessor) &&
                        isAnyAssocAttrChanged(postAfterAccessor)) {
                        /* Get the associated referenced entity */
                        Object obj = getAttribute(postAfterAccessor);
                        if (obj instanceof Entity) {
                            Entity refEntity = (Entity)obj;
                            /* And if it's post-status is NEW */
                            if (refEntity.getPostState() == STATUS_NEW) {
                                /*
                                 * Post the referenced entity first, before
                                 * posting this entity.
                                 */
                                refEntity.postChanges(e);
                            }
                        }
                    }
                }
            }
        }
        super.postChanges(e);
    }
    /*
     * Returns true if association named by the accessor passed in is
     * a composition
     */

    private boolean isComposition(String accessorName) {
        AttributeDef assocAttrDef =
            getEntityDef().findAttributeDef(accessorName);
        return ((AssociationDefImpl)assocAttrDef).hasContainer();
    }
    /*
     * Returns true if any attribute in the current entity object, which is
     * involved in the association named by the accessor passed in, has
     * changed its value in the current transaction.
     */

    private boolean isAnyAssocAttrChanged(String accessorName) {
        AssociationDefImpl attr =
            (AssociationDefImpl)getEntityDef().findAttributeDef(accessorName);
        AttributeDefImpl[] attrsInAssoc = attr.getAttributeDefImpls();
        boolean anyAssocAttrChanged = false;
        for (AttributeDefImpl assocAttrDef : attrsInAssoc) {
            if (isAttributeChanged(assocAttrDef.getIndex())) {
                anyAssocAttrChanged = true;
                break;
            }
        }
        return anyAssocAttrChanged;
    }
    /*
     * Returns true if any attribute in the current entity object, which is
     * involved in the association named by the accessor passed in, has
     * changed its value in the current transaction.
     */

    private String[] getPostAfterAccessors() {
        EntityDefImpl eDef = getEntityDef();
        String postAfterAssocList =
            (String)eDef.getProperty(POST_AFTER_ACCESSORS);
        if (postAfterAssocList != null && postAfterAssocList.length() > 0) {
            return postAfterAssocList.split(REGEX_COMMA_WITH_OPT_WHITESPACE);
        }
        return null;
    }

    /*
     * This is for security functionality method
     * to get from session data
     */

    public Object getFromSession(String key) {

        return getDBTransaction().getSession().getUserData().get(key);
    }

    /*
     * This is for security functionality method
     * to set from session data
     */

    public void setToSession(String key, Object val) {

        Hashtable userData = getDBTransaction().getSession().getUserData();
        if (userData == null) {
            userData = new Hashtable();
        }

        userData.put(key, val);
    }

    /* This is check Attribute value that already changed or not
     * */

    public boolean checkAttributeValue(String _attribute) {
        AttributeDefImpl _checkAttr =
            (AttributeDefImpl)getEntityDef().findAttributeDef(_attribute);
        return isAttributeChanged(_checkAttr.getIndex());
    }

}
