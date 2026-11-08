import DAO.ThanhToanKhuyenMaiDAO;
import java.math.BigDecimal;

/** Chạy: javac -d build src/.../ThanhToanKhuyenMaiDAO.java tests/TestTinhGiamGia.java && java -cp build TestTinhGiamGia */
public class TestTinhGiamGia {
    private static void expect(String tienGoc,String loai,String muc,String expected){
        BigDecimal result=ThanhToanKhuyenMaiDAO.tinhGiam(new BigDecimal(tienGoc),loai,new BigDecimal(muc));
        if(result.compareTo(new BigDecimal(expected))!=0)
            throw new AssertionError(tienGoc+" / "+loai+" / "+muc+" -> "+result+" != "+expected);
    }
    public static void main(String[] args){
        expect("1000000","PHAN_TRAM","20","200000");
        expect("1000000","SO_TIEN","300000","300000");
        expect("100000","SO_TIEN","300000","100000");
        expect("150005","PHAN_TRAM","5","7500");
        expect("1000000","PHAN_TRAM","100","1000000");
        System.out.println("PASS: 5 kiểm thử công thức giảm tiền.");
    }
}
