package app.ossb.prim.bean.extensions;

import java.sql.CallableStatement;

import java.sql.PreparedStatement;

import oracle.jbo.domain.Number;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import java.util.StringTokenizer;

import oracle.jbo.JboException;
import oracle.jbo.Row;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewObject;
import oracle.jbo.server.ApplicationModuleImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Custom ADF Framework Extension Class for Entity Definitions
 * @author Hadi Wijaya
 * @version 1.0
 */
public class CustomApplicationModuleImpl extends ApplicationModuleImpl implements CustomApplicationModule {
    // Add your custom code here or use the
    // Source | Override Methods... dialog to override
    // methods in the base class.

    public void callProcWithNoArgs() {

    }


    //----------------[ Begin Helper Code ]------------------------------

    /**
     * Simplifies calling a stored procedure with bind variables
     *
     * NOTE: If you want to invoke a stored procedure without any bind variables
     * ====  then you can just use the basic getDBTransaction().executeCommand()
     *
     * @param stmt stored procedure statement to execute
     * @param bindVars Object array of parameters
     */
    protected void callStoredProcedure(String stmt, Object[] bindVars) {
        PreparedStatement st = null;
        try {
            st =
 getDBTransaction().createPreparedStatement("begin " + stmt + "; end;", 0);
            if (bindVars != null) {
                for (int z = 0; z < bindVars.length; z++) {
                    st.setObject(z + 1, bindVars[z]);
                }
            }
            st.executeUpdate();
        } catch (SQLException e) {
            throw new JboException(e);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Simplifies calling a stored function with bind variables
     *
     * You can use the NUMBER, DATE, and VARCHAR2 constants in this
     * class to indicate the function return type for these three common types,
     * otherwise use one of the JDBC types in the java.sql.Types class.
     *
     * NOTE: If you want to invoke a stored procedure without any bind variables
     * ====  then you can just use the basic getDBTransaction().executeCommand()
     *
     * @param sqlReturnType JDBC datatype constant of function return value
     * @param stmt stored function statement
     * @param bindVars Object array of parameters
     * @return function return value as an Object
     */
    protected Object callStoredFunction(int sqlReturnType, String stmt,
                                        Object[] bindVars) {
        CallableStatement st = null;
        try {
            st =
 getDBTransaction().createCallableStatement("begin ? := " + stmt + "; end;",
                                            0);
            st.registerOutParameter(1, sqlReturnType);
            if (bindVars != null) {
                for (int z = 0; z < bindVars.length; z++) {
                    st.setObject(z + 2, bindVars[z]);
                }
            }
            st.executeUpdate();
            return st.getObject(1);
        } catch (SQLException e) {
            throw new JboException(e);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //----------------[ End Helper Code ]------------------------------


    //-------[Start Security function]-------------------------------------

    public Object getFromSession(String key) {

        return getDBTransaction().getSession().getUserData().get(key);
    }

    public void setToSession(String key, Object val) {

        Hashtable userData = getDBTransaction().getSession().getUserData();
        if (userData == null) {
            userData = new Hashtable();
        }
        //System.err.println("########## SESSION ###########");
        if(key != null && val != null){
            //System.err.println("[" + key + "] has value [" + val + "]");
        }
        if(key == null){
            //System.err.println("KEY IS NULL");    
        }
        if(val == null){
            //System.err.println("[" + key + "] WITH VALUE IS NULL");
        }
        //System.err.println("########## END SESSION ###########");
        
        if(val != null)
            userData.put(key, val);
    }

    public String getLoginFromSession(String key) {

        return (String)getDBTransaction().getSession().getUserData().get(key);
    }

    public void setLoginToSession(String key, String val) {

        Hashtable userData = getDBTransaction().getSession().getUserData();
        if (userData == null) {
            userData = new Hashtable();
        }

        userData.put(key, val);
    }

    @Override
    protected void passivateState(Document doc, Element parent) {
       // System.out.println("### PASSIVATION ###");

        Node nodeUserData = doc.createElement("USERDATA");
        Hashtable hs = getDBTransaction().getSession().getUserData();

        if (hs != null) {

            Set mapKeys = hs.keySet();
            Iterator hsKeysIter = mapKeys.iterator();
            while (hsKeysIter.hasNext()) {

                String key = (String)hsKeysIter.next();
                if (!key.startsWith("__") && !key.startsWith("jbo")) {
                    Object obj = hs.get(key);
                    String value = obj.toString();
                    String type = getUserdataType(obj);
                    Element elem = doc.createElement(key);
                    elem.setAttribute("KEY", key);
                    elem.setAttribute("VALUE", value);
                    elem.setAttribute("TYPE", type);

                    nodeUserData.appendChild(elem);
                    //System.out.println("KEY MATCH:" + key+" "+value+" "+type);
                }                
                //System.out.println("KEY NO MATCH:" + key);
            }
        }

        parent.appendChild(nodeUserData);
        super.passivateState(doc, parent);
    }

    @Override
    protected void activateState(Element parent) {
        super.activateState(parent);

       // System.out.println("### ACTIVATION ###");

        NodeList nl = parent.getElementsByTagName("USERDATA");
        if (nl.getLength() > 0) {

            Node n = nl.item(0);
            NodeList nl2 = n.getChildNodes();

            for (int i = 0; i < nl2.getLength(); i++) {

                Element e = (Element)nl2.item(i);
                String key = e.getAttribute("KEY");
                String value = e.getAttribute("VALUE");
                String type = e.getAttribute("TYPE");

                //System.out.println("[" + key + "] has value [" + value + "]. Java type is " + type);

                if (!key.startsWith("__") && !key.startsWith("jbo")) {
                    try {
                        Object obj = convertUserdataType(value, type);
                        setToSession(key, obj);

                    } catch (SQLException ex) {
                        new JboException(ex.getMessage());
                    }
                }
            }
        }
    }

    protected String getUserdataType(Object o) {

        String type = "";
        if (o != null){
            if (o instanceof String)
                type = "STRING";
    
            if (o instanceof Number)
                type = "NUMBER";
    
            if (o instanceof Integer)
                type = "INTEGER";
        }
        
        return type;
    }


    protected Object convertUserdataType(String value,
                                         String type) throws SQLException {

        Object o = null;
        if(type != null && value != null){
            if (type.equals("STRING"))
                o = value;
    
            if (type.equals("NUMBER"))
                o = new Number(value);
    
            if (type.equals("INTEGER"))
                o = Integer.valueOf(value);
        }
        return o;
    }

    //-------[End Security function]-------------------------------------

    public String getInformation(String _viewRef, String _attributeDisplay) {
        CustomViewObjectImpl _view =
            (CustomViewObjectImpl)this.findViewObject(_viewRef);
        CustomViewRowImpl _row = (CustomViewRowImpl)_view.getCurrentRow();

        String output = "";
        if (_attributeDisplay != null) {
            StringTokenizer st = new StringTokenizer(_attributeDisplay, ",");
            if (st.countTokens() > 1) {
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if (_row.getAttribute(token) != null) {
                        output += (String)_row.getAttribute(token);
                        if (st.hasMoreTokens())
                            output += " ";
                    }
                }
            } else {
                String token = st.nextToken();
                if (_row.getAttribute(token) != null) {
                    output = (String)_row.getAttribute(token);
                }
            }
            return output;
        } else {
            return output;
        }
    }
        
    public List<String> queryAbbrIds(String filter, boolean doFilterInMemory, Number _empId){
    
        //ViewObject abbrVO = this.findViewObject("Abbreviation");
        ViewObject abbrVO = this.findViewObject("AbbreviationAll");
        abbrVO.setNamedWhereClauseParam("empId", _empId);
        //ViewCriteria jobFilterCriteria = abbrVO.getViewCriteriaManager().getViewCriteria("AbbreviationVOCriteria");
        ViewCriteria jobFilterCriteria = abbrVO.getViewCriteriaManager().getViewCriteria("AbbreviationAllVOCriteria");
        abbrVO.setNamedWhereClauseParam("srchFilterStr",filter!=null && filter.length()>0?filter:"%");
        
        if(doFilterInMemory){
       // System.out.println("CACHE");
            jobFilterCriteria.setCriteriaMode(ViewCriteria.CRITERIA_MODE_CACHE);

        }
        else{
           // System.out.println("QUERY");
            jobFilterCriteria.setCriteriaMode(ViewCriteria.CRITERIA_MODE_QUERY);
        }
        abbrVO.applyViewCriteria(jobFilterCriteria);
        abbrVO.executeQuery();
        
        List<String> rwlist = new ArrayList<String>();
        Row first = abbrVO.first();
        if (first!=null){
            rwlist.add((String)first.getAttribute("Description"));
            while (abbrVO.hasNext()) {
               // System.out.println("*");
                rwlist.add((String)abbrVO.next().getAttribute("Description"));
            }
        }
        
        return rwlist;
    }
}
