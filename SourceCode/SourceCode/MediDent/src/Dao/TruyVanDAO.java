package Dao;
import ConnectDB.KetNoi;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
/** DAO chung chỉ đọc/ghi DB, không điều khiển Swing. */
public final class TruyVanDAO {
    private TruyVanDAO(){
    }
    @FunctionalInterface public interface Work<T>{
        T run(Connection c) throws Exception;
    }
    public static <T>T tx(Work<T> work){
        try(Connection c=KetNoi.mo()){
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            c.setAutoCommit(false);
            try{
                T result=work.run(c);
                c.commit();
                return result;
            }
            catch(Exception e){
                c.rollback();
                throw bad(e);
            }
        }
        catch(Exception e){
            throw bad(e);
        }
    }
    public static RuntimeException bad(Exception e){
        return e instanceof RuntimeException r?r:new IllegalStateException(e.getMessage()==null?e.getClass().getSimpleName():e.getMessage(),e);
    }
    public static void bind(PreparedStatement ps,Object... params)throws SQLException{
        for(int i=0;i<params.length;i++){
            Object v=params[i];
            if(v instanceof java.time.LocalDate x)ps.setDate(i+1,java.sql.Date.valueOf(x));
            else if(v instanceof java.time.LocalTime x)ps.setTime(i+1,Time.valueOf(x));
            else if(v instanceof java.math.BigDecimal x)ps.setBigDecimal(i+1,x);
            else if(v==null)ps.setObject(i+1,null);
            else ps.setObject(i+1,v);
        }
    }
    public static List<Map<String,Object>> select(Connection c,String query,Object... params)throws SQLException{
        try(PreparedStatement ps=c.prepareStatement(query)){
            bind(ps,params);
            try(ResultSet rs=ps.executeQuery()){
                List<Map<String,Object>> out=new ArrayList<>();
                ResultSetMetaData md=rs.getMetaData();
                while(rs.next()){
                    Map<String,Object> row=new LinkedHashMap<>();
                    for(int i=1;i<=md.getColumnCount();i++)row.put(md.getColumnLabel(i),rs.getObject(i));
                    out.add(row);
                }
                return out;
            }
        }
    }
    public static List<Map<String,Object>> list(String query,Object...params){
        try(Connection c=KetNoi.mo()){
            return select(c,query,params);
        }
        catch(Exception e){
            throw bad(e);
        }
    }
    public static Map<String,Object> one(Connection c,String query,Object...params)throws SQLException{
        var x=select(c,query,params);
        return x.isEmpty()?null:x.get(0);
    }
    public static Map<String,Object> first(String query,Object...params){
        var x=list(query,params);
        return x.isEmpty()?null:x.get(0);
    }
    public static int update(Connection c,String sql,Object... params)throws SQLException{
        try(PreparedStatement ps=c.prepareStatement(sql)){
            bind(ps,params);
            return ps.executeUpdate();
        }
    }
    public static int insert(Connection c,String sql,Object... params)throws SQLException{
        try(PreparedStatement ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            bind(ps,params);
            if(ps.executeUpdate()!=1)throw new SQLException("Không ghi được bản ghi mới");
            try(ResultSet keys=ps.getGeneratedKeys()){
                if(keys.next())return keys.getInt(1);
            }
        }
        throw new SQLException("Không lấy được mã tự tăng");
    }
    public static int n(Object o){
        return o==null?0:((Number)o).intValue();
    }
    public static String s(Object o){
        return o==null?"":String.valueOf(o);
    }
    public static java.math.BigDecimal money(Object o){
        return o==null?java.math.BigDecimal.ZERO:new java.math.BigDecimal(o.toString());
    }
    public static void exactlyOne(int n,String message)throws SQLException{
        if(n!=1)throw new SQLException(message);
    }
    public static void audit(Connection c,int actor,String action,String target,String detail)throws SQLException{
        update(c,"INSERT INTO dbo.NhatKyHeThong(hanhDong,doiTuong,moTa) VALUES(?,?,?)",action,target,"maTK="+actor+"; "+detail);
    }
}
