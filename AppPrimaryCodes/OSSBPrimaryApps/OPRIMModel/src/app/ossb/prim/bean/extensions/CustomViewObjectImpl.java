package app.ossb.prim.bean.extensions;

import java.util.Hashtable;
import oracle.jbo.AttributeDef;
import oracle.jbo.Key;
import oracle.jbo.NoDefException;
import oracle.jbo.Row;
import oracle.jbo.Variable;
import oracle.jbo.VariableManager;
import oracle.jbo.server.ViewAttributeDefImpl;
import oracle.jbo.server.ViewDefImpl;
import oracle.jbo.server.ViewObjectImpl;

/**
 * Custom ADF Framework Extension Class for View Object Components
 * @author Hadi Wijaya
 * @version 1.0
 */

public class CustomViewObjectImpl extends ViewObjectImpl {
  private static final String INSERT_NEW_ROWS_AT_END = "InsertNewRowsAtEnd";
  
  @Override
  protected void create() {
    super.create();
    if(isReadOnlyNonEntitySQLViewWithAtLeastOneKeyAttribute()){
      setManageRowsByKey(true);
    }
  }

  /**
   * @return true is View Object is ReadOnly and at least has one key attribute 
   * (primary key)
   */
  boolean isReadOnlyNonEntitySQLViewWithAtLeastOneKeyAttribute() {
    if (getViewDef().isFullSql() && getEntityDefs() == null){
      for(AttributeDef attrDef : getAttributeDefs()){
        if(attrDef.isPrimaryKey()){
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void insertRow(Row row) {
    super.insertRow(row);
    if(getProperty(INSERT_NEW_ROWS_AT_END) != null){
      row.removeAndRetain();
      last();
      next();
      getDefaultRowSet().insertRow(row);
    }
  }
  
  /**
   * Helper Method
   */
  protected void clearWhereState(){
    ViewDefImpl viewDef = getViewDef();
    Variable[] viewInstanceVars = null;
    VariableManager viewInstanceVarMgr = ensureVariableManager();
    if(viewInstanceVarMgr != null){
      viewInstanceVars = viewInstanceVarMgr.getVariablesOfKind(Variable.VAR_KIND_WHERE_CLAUSE_PARAM);
      if(viewInstanceVars != null){
        for(Variable v : viewInstanceVars){
          //only remove the variable if its not on the view def.
          if(!hasViewDefVariableNamed(v.getName())){
            removeNamedWhereClauseParam(v.getName());
          }
        }
      }
    }
  }
  
  private boolean hasViewDefVariableNamed(String name){
    boolean ret = false;
    VariableManager viewDefVarMgr = getViewDef().ensureVariableManager();
    if(viewDefVarMgr != null){
      try{
        ret = viewDefVarMgr.findVariable(name) != null;
      }
      catch(NoDefException ex){
        //ignore
      }
    }
    return ret;
  }

  /**
   * refresh-but-stay-where-you-were functionality
   * Steve Muench http://radio.weblogs.com/0118231/2004/11/22.html
   */
  public void refreshQueryKeepingCurrentRow() {
    Row currentRow = getCurrentRow();
    Key currentRowKey = currentRow.getKey();
    int rangePosOfCurrentRow = getRangeIndexOf(currentRow);
    int rangeStartBeforeQuery = getRangeStart();
    executeQuery();
    setRangeStart(rangeStartBeforeQuery);
    if (rangePosOfCurrentRow >= 0) {
      findAndSetCurrentRowByKey(currentRowKey, rangePosOfCurrentRow);
    }
  }

    /*
     * This is for security functionality method
     * to get from session data
     */
    public Object getFromSession(String key){
        
        return getDBTransaction().getSession().getUserData().get(key);    
    }
    
    /*
     * This is for security functionality method
     * to set from session data
     */    
    public void setToSession(String key, Object val){
    
        Hashtable userData = getDBTransaction().getSession().getUserData();
        if (userData == null) {
            userData = new Hashtable();
        }
        
        userData.put(key, val);
    }
  
  public String getUnderlyingTable(int attrIndex) {
        String underlyingTable = null;
        ViewAttributeDefImpl attr = getViewAttributeDefImpls()[attrIndex];
        if (attr.getAttributeKind() == attr.ATTR_PERSISTENT) {
            underlyingTable = attr.getEntityDef().getSource();
        }
        return underlyingTable;
    }

}
