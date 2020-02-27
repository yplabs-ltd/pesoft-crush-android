package net.pesofts.crush.Util;

/**
 * Created by zerowns on 2016-05-30.
 */
public class HappyCallUtil {

    static{
        key = new byte[]{
                56,116,38,22,7,47,107,16,8,41,
                15,114,3,11,30,22,4,88,21,87,
                112,32,113,120,10,22,6,17,125,55,
                22,20,9,100,15,118
        };
    }
    public static byte[] key;

    /**
     * XOR 연산임. 메소드명은 그냥 난독화
     * @param a     데이타
     * @param key   복호화키
     * @return      키값
     */
    public static byte[] save(byte[] a, byte[] key) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ key[i%key.length]);
        }
        return out;
    }

    /**
     *  그냥 난독화 겸 -_-;
     * @param __
     * @param _
     * @param ____
     * @param ___
     * @return
     */
    public static String call(String __,String _,String ____,String ___){
        return __+___+_+____;
    }
}
