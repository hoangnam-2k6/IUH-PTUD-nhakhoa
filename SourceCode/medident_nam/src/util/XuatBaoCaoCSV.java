package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

/** Xuất CSV UTF-8 có BOM; bảo vệ ký tự đặc biệt và công thức bảng tính. */
public final class XuatBaoCaoCSV {
    private XuatBaoCaoCSV() { }

    public static void xuat(Path tep,String loai,String khoang,String[] cot,List<Object[]> duLieu) throws IOException {
        try(BufferedWriter out=Files.newBufferedWriter(tep,StandardCharsets.UTF_8)){
            out.write('\uFEFF');
            ghiDong(out,new Object[]{"Loại báo cáo",loai});
            ghiDong(out,new Object[]{"Khoảng thời gian",khoang});
            ghiDong(out,cot);
            for(Object[] dong:duLieu)ghiDong(out,dong);
        }
    }

    private static void ghiDong(Writer out,Object[] dong) throws IOException {
        for(int i=0;i<dong.length;i++){
            if(i>0)out.write(',');
            String text=dong[i]==null?"":dong[i].toString();
            if(!text.isEmpty() && "=+-@".indexOf(text.charAt(0))>=0)text="'"+text;
            out.write('"');out.write(text.replace("\"","\"\""));out.write('"');
        }
        out.write('\n');
    }
}
