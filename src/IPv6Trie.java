import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 使用字典树存储，实现方式采用数组
 */
public class IPv6Trie {

    private int size;
    private int allocatedSize;
    private int left[], right[];
    private int[] value;
    public static final int INIT_SIZE = 1024;
    public static final String CSV_PATH = "e:/GeoLite2-Country-Blocks-IPv6.csv"; //IPv6数据文件，测试时作为库文件写入到字典树中

    public IPv6Trie() {
        init(INIT_SIZE);
    }

    /**
     * 初始化
     *
     * @param size
     */
    private void init(int size) {
        this.allocatedSize = size;
        left = new int[allocatedSize];
        right = new int[allocatedSize];
        value = new int[allocatedSize];
        this.size = 1;
        left[0] = -1;
        right[0] = -1;
        value[0] = -1;
    }

    /**
     * 存储IPv6地址和对应的位置信息到字典树中
     *
     * @param IPv6Address IPv6地址
     * @param text        IPv6地址对应的位置信息
     */
    public void put(String IPv6Address, int text) {

        //获取网络前缀
        int index = IPv6Address.indexOf("/");
        int length = Integer.parseInt(IPv6Address.substring(index + 1, IPv6Address.length()));

        //获取不带/的IPv6地址
        String addr = IPv6Address.substring(0, index);
        //获取解压缩后的ipv6地址
        String address = decompress(addr).replaceAll(":", "");
        //System.out.println(address);

        //将二进制串转化为字符数组
        char[] addressChars = getBinaryIPv6Address(address).toCharArray();

        int node = 0;
        int counter = 0;
        for (char ch : addressChars) {
            int digit = Integer.parseInt(ch + "", 2);
            int initTest = 1;
            counter++;
            boolean result = ((digit & initTest) != 0);
            int next = result ? right[node] : left[node];
            if (next == -1) {//没有节点
                next = size;
                if (next == allocatedSize) {
                    expandAllocatedSize();
                }
                if (result) {
                    right[node] = next;
                } else {
                    left[node] = next;
                }
                value[next] = -1;
                left[next] = -1;
                right[next] = -1;
                node = next;
                size++;
            } else { //有节点
                node = next;
            }

            if(counter >= length){
                break;
            }
        }
        value[node] = text;
    }

    /**
     * 根据IPv6地址，获取对应的位置信息
     *
     * @param IPv6Address IPv6地址
     * @return 对应的位置信息
     */
    public int get(String IPv6Address) {
        String address = decompress(IPv6Address).replaceAll(":", "");
        char[] addressChars = getBinaryIPv6Address(address).toCharArray();
        int node = 0;
        int returnValue = -1;
        int counter = 0;
        for (char ch : addressChars) {
            int digit = Integer.parseInt(ch + "", 16);
            int initTest = 1;
            counter++;
            if(node != -1) {

                boolean result = ((digit & initTest) != 0);
                if (result) {
                    node = right[node];
                } else {
                    node = left[node];
                }
                if ((node != -1) && (value[node]!=-1)) {
                    returnValue = value[node];
                }
            }
        }
        return returnValue;
    }

    /**
     *  将IPv6地址转换为二进制串
     * @param depressedAddress 不带/和:的解压缩的16进制IPv6地址  2606470000000000000000006812ec4b
     * @return 二进制字符串
     */
    private String getBinaryIPv6Address(String depressedAddress){
        StringBuilder bString = new StringBuilder();
        String tmp;
        for (int i = 0; i < depressedAddress.length(); i++) {
            tmp = "0000" + Integer.toBinaryString(Integer.parseInt(depressedAddress.substring(i, i + 1), 16));
            bString.append(tmp.substring(tmp.length() - 4));
        }
        return bString.toString();
    }

    /**
     * 扩充字典树节点数组
     */
    private void expandAllocatedSize() {
        int oldSize = allocatedSize;
        allocatedSize = allocatedSize * 2;

        int[] newLeft = new int[allocatedSize];
        System.arraycopy(left, 0, newLeft, 0, oldSize);
        left = newLeft;

        int[] newRight = new int[allocatedSize];
        System.arraycopy(right, 0, newRight, 0, oldSize);
        right = newRight;

        int[] newValue = new int[allocatedSize];
        System.arraycopy(value, 0, newValue, 0, oldSize);
        value = newValue;
    }


    /**
     * 将IPv6地址解压缩
     *
     * @param iPv6Address 原始IPv6地址，可能被压缩
     * @return 返回未被压缩的IPv6地址 xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx
     */
    private static String decompress(String iPv6Address) {
        //System.out.println("before decompress: " + iPv6Address);
        int left = 0, right = 0;
        StringBuilder str = new StringBuilder();
        if (iPv6Address.contains("::")) {
            String[] address = iPv6Address.split("::");
            if (address.length == 0) { //::
                str.append("0000:0000:0000:0000:0000:0000:0000:0000");

                return str.toString();
            } else if (address.length == 2) { //::1或1::1

                if (!address[0].equals("")) {
                    if (address[0].contains(":")) {
                        left = address[0].split(":").length;
                    } else {
                        left = 1;
                    }
                }

                if (address[1].contains(":")) {
                    right = address[1].split(":").length;
                } else {
                    right = 1;
                }


                if (!address[0].equals("")) {
                    str.append(address[0]).append(":");
                }
                for (int i = 1; i <= 8 - right - left; i++) {
                    str.append("0000:");
                }

                str.append(address[1]);

            } else if (address.length == 1) { //1::
                if (!address[0].equals("")) {
                    if (address[0].contains(":")) {
                        left = address[0].split(":").length;
                    } else {
                        left = 1;
                    }
                }
                str.append(address[0]).append(":");
                right = 0;
                for (int i = 1; i <= 8 - right - left; i++) {
                    str.append("0000:");
                }
            }

        } else { //不包含::
            str.append(iPv6Address);
        }
        String address = str.toString();
        String[] items = address.split(":");
        StringBuilder stringBuilder = new StringBuilder();
        for (String item : items) {
            if (item.equals("0")) {
                stringBuilder.append("0000:");
            } else {
                for (int i = 4 - item.length(); i > 0; i--) {
                    stringBuilder.append("0");
                }
                stringBuilder.append(item).append(":");
            }

        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        //System.out.println("depressedAddress: " + stringBuilder.toString());
        return stringBuilder.toString();


    }

    public static void main(String[] args) {
        IPv6Trie iPv6Trie = new IPv6Trie();
        iPv6Trie.put("2606:4700::6812:ec4b/32",1);
        iPv6Trie.put("2606:4700::6812:ec4b/64",2);
        iPv6Trie.put("2606:4700::6812:ec4b/126",5);
        System.out.println(iPv6Trie.get("2606:4700::6812:ec4b"));

        /*try {
            BufferedReader br = new BufferedReader(new FileReader(CSV_PATH));

            String line ;
            br.readLine();
            while((line = br.readLine())!=null){
                iPv6Trie.put(line.split(",.*")[0], Integer.parseInt(line.split(",")[1]));

            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(iPv6Trie.get("2c0f:feb0:8000::/33"));*/

    }
}
