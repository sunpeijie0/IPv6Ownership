/**
 *  使用字典树存储，实现方式采用数组
 */
public class IPv6Trie {

    private int size;
    private int allocatedSize;
    private int left[], right[], value[];

    public IPv6Trie() {
        init(1024);
    }

    /**
     *  初始化
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
     *  存储IPv6地址和对应的位置信息到字典树中
     * @param IPv6Address IPv6地址
     * @param text IPv6地址对应的位置信息
     */
    public void put(String IPv6Address, int text) {

        String address = decompress(IPv6Address).replaceAll(":", "").substring(0,16);
        char[] addressChars = address.toCharArray();
        int node = 0;
        for (char ch : addressChars) {
            int digit = Integer.parseInt(ch + "", 16);
            int initTest = 8;
            while (initTest >= 1) {
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
                initTest = initTest >> 1;
            }
        }
        value[node] = text;
    }

    /**
     *  根据IPv6地址，获取对应的位置信息
     * @param IPv6Address IPv6地址
     * @return 对应的位置信息
     */
    public int get(String IPv6Address) {
        String address = decompress(IPv6Address).replaceAll(":", "").substring(0,16);
        char[] addressChars = address.toCharArray();
        int node = 0;
        int returnValue = -1;
        for (char ch : addressChars) {
            int digit = Integer.parseInt(ch + "", 16);
            int initTest = 8;
            while (initTest >= 1 && node != -1) {

                boolean result = ((digit & initTest) != 0);
                if(result){
                    node = right[node];
                }else {
                    node = left[node];
                }
                if((node != -1) && (value[node] != -1)){
                    returnValue = value[node];
                }
                initTest = initTest >> 1;
            }

        }
        return returnValue;
    }

    /**
     *  扩充字典树节点数组
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
        int left = 0, right = 0;
        StringBuilder str = new StringBuilder();
        if (iPv6Address.contains("::")) {
            String[] address = iPv6Address.split("::");
            if (!address[0].equals("")) {
                if (address[0].contains(":")) {
                    left = address[0].split(":").length;
                } else {
                    left = 1;
                }
            }
            if (!address[1].equals("")) {
                if (address[1].contains(":")) {
                    right = address[1].split(":").length;
                } else {
                    right = 1;
                }
            }

            if (!address[0].equals("")) {
                str.append(address[0]).append(":");
            }
            for (int i = 1; i <= 8 - right - left; i++) {
                str.append("0000:");
            }
            if (!address[1].equals("")) {
                str.append(address[1]);
            }

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
        return stringBuilder.toString();


    }

    public static void main(String[] args) {
        IPv6Trie iPv6Trie = new IPv6Trie();
        iPv6Trie.put("1111::103:20:0:A211",123);
        System.out.println(iPv6Trie.get("1111::104:20:0:A211"));
        //put("1111::103:20:0:A211", "123");
    }
}
