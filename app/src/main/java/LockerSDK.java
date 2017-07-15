import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class LockerSDK {
    private OutputStream output;
    private InputStream input;
    private int timeout;

    public LockerSDK(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
        this.timeout = 30;
    }

    public int timeout_set(int timeout) {
        this.timeout = timeout * 10;
        return this.timeout;
    }

    public byte[] scan() {
        return this.scan((byte)0, (byte)15);
    }

    public byte[] scan(byte start, byte stop) {
        if ((this.output != null ? this.input : null) == null) {
            return null;
        }
        int tmp = this.timeout;
        ArrayList<Byte> result = new ArrayList<Byte>(0);
        int i = 0;
        int $gensym1 = stop - start + 1;
        if (i < $gensym1) {
            do {
                if (!this.is_online((byte)(start + i))) continue;
                ArrayList<Byte> $gensym2 = result;
                $gensym2.add(new Byte((byte)(start + i)));
            } while (++i < $gensym1);
        }
        this.timeout = tmp;
        ArrayList<Byte> $gensym3 = result;
        byte[] $gensym4 = new byte[$gensym3.size()];
        int $gensym6 = 0;
        for (Byte $gensym5 : $gensym3) {
            $gensym4[$gensym6] = $gensym5.byteValue();
            ++$gensym6;
        }
        return $gensym4;
    }

    public boolean is_online(byte board) {
        if ((this.output != null ? this.input : null) == null) {
            return false;
        }
        int[] tmp = this.query(board);
        if (tmp == null) {
            return false;
        }
        return true;
    }

    public int query(byte board, byte lock) {
        if ((this.output != null ? this.input : null) == null) {
            return -1;
        }
        int[] states = this.query(board);
        if (states == null) {
            return -1;
        }
        return states[lock];
    }

    public int[] query(byte board) {
        int[] $gensym11;
        byte[] $gensym9;
        if ((this.output != null ? this.input : null) == null) {
            return null;
        }
        int parity = board ^ 2 ^ 0 ^ 0 ^ 0;
        byte[] $gensym8 = $gensym9 = new byte[8];
        $gensym8[0] = (byte)170;
        $gensym8[1] = board;
        $gensym8[2] = (byte)2;
        $gensym8[3] = 0;
        $gensym8[4] = 0;
        $gensym8[5] = 0;
        $gensym8[6] = (byte)parity;
        $gensym8[7] = (byte)85;
        byte[] request = $gensym9;
        byte[] response = this._send(request, 8);
        if (response == null) {
            return null;
        }
        byte s1 = response[3];
        byte s2 = response[4];
        byte s3 = response[5];
        int[] $gensym10 = $gensym11 = new int[25];
        $gensym10[0] = 0;
        $gensym10[1] = 0;
        $gensym10[2] = 0;
        $gensym10[3] = 0;
        $gensym10[4] = 0;
        $gensym10[5] = 0;
        $gensym10[6] = 0;
        $gensym10[7] = 0;
        $gensym10[8] = 0;
        $gensym10[9] = 0;
        $gensym10[10] = 0;
        $gensym10[11] = 0;
        $gensym10[12] = 0;
        $gensym10[13] = 0;
        $gensym10[14] = 0;
        $gensym10[15] = 0;
        $gensym10[16] = 0;
        $gensym10[17] = 0;
        $gensym10[18] = 0;
        $gensym10[19] = 0;
        $gensym10[20] = 0;
        $gensym10[21] = 0;
        $gensym10[22] = 0;
        $gensym10[23] = 0;
        $gensym10[24] = 0;
        int[] result = $gensym11;
        byte mask = 1;
        int i = 0;
        int $gensym12 = 8;
        if (i < $gensym12) {
            do {
                if ((s3 & mask << i) != 0) continue;
                result[i + 1] = 1;
            } while (++i < $gensym12);
        }
        mask = 1;
        i = 0;
        int $gensym13 = 8;
        if (i < $gensym13) {
            do {
                if ((s2 & mask << i) != 0) continue;
                result[i + 1 + 8] = 1;
            } while (++i < $gensym13);
        }
        mask = 1;
        i = 0;
        int $gensym14 = 8;
        if (i < $gensym14) {
            do {
                if ((s1 & mask << i) != 0) continue;
                result[i + 1 + 8 + 8] = 1;
            } while (++i < $gensym14);
        }
        return result;
    }

    public boolean open(byte board, byte lock) {
        byte[] $gensym16;
        if ((this.output != null ? this.input : null) == null) {
            return false;
        }
        int parity = board ^ 1 ^ 0 ^ 0 ^ lock;
        byte[] $gensym15 = $gensym16 = new byte[8];
        $gensym15[0] = (byte)170;
        $gensym15[1] = board;
        $gensym15[2] = 1;
        $gensym15[3] = 0;
        $gensym15[4] = 0;
        $gensym15[5] = lock;
        $gensym15[6] = (byte)parity;
        $gensym15[7] = (byte)85;
        byte[] request = $gensym16;
        byte[] response = this._send(request, 8);
        boolean bl = response != null ? response.length == 8 : false;
        if (bl) {
            int i = 0;
            int $gensym17 = 8;
            if (i < $gensym17) {
                do {
                    if (request[i] == response[i]) continue;
                    return false;
                } while (++i < $gensym17);
            }
            return true;
        }
        return false;
    }

    public int check(byte board, byte box) {
        if ((this.output != null ? this.input : null) == null) {
            return -1;
        }
        int[] states = this.query(board);
        if (states == null) {
            return -1;
        }
        return states[box];
    }

    public int[] check(byte board) {
        int[] $gensym21;
        byte[] $gensym19;
        if ((this.output != null ? this.input : null) == null) {
            return null;
        }
        int parity = board ^ 3 ^ 0 ^ 0 ^ 0;
        byte[] $gensym18 = $gensym19 = new byte[8];
        $gensym18[0] = (byte)170;
        $gensym18[1] = board;
        $gensym18[2] = (byte)3;
        $gensym18[3] = 0;
        $gensym18[4] = 0;
        $gensym18[5] = 0;
        $gensym18[6] = (byte)parity;
        $gensym18[7] = (byte)85;
        byte[] request = $gensym19;
        byte[] response = this._send(request, 8);
        if (response == null) {
            return null;
        }
        byte s1 = response[3];
        byte s2 = response[4];
        byte s3 = response[5];
        int[] $gensym20 = $gensym21 = new int[25];
        $gensym20[0] = 0;
        $gensym20[1] = 0;
        $gensym20[2] = 0;
        $gensym20[3] = 0;
        $gensym20[4] = 0;
        $gensym20[5] = 0;
        $gensym20[6] = 0;
        $gensym20[7] = 0;
        $gensym20[8] = 0;
        $gensym20[9] = 0;
        $gensym20[10] = 0;
        $gensym20[11] = 0;
        $gensym20[12] = 0;
        $gensym20[13] = 0;
        $gensym20[14] = 0;
        $gensym20[15] = 0;
        $gensym20[16] = 0;
        $gensym20[17] = 0;
        $gensym20[18] = 0;
        $gensym20[19] = 0;
        $gensym20[20] = 0;
        $gensym20[21] = 0;
        $gensym20[22] = 0;
        $gensym20[23] = 0;
        $gensym20[24] = 0;
        int[] result = $gensym21;
        byte mask = 1;
        int i = 0;
        int $gensym22 = 8;
        if (i < $gensym22) {
            do {
                if ((s3 & mask << i) == 0) continue;
                result[i + 1] = 1;
            } while (++i < $gensym22);
        }
        mask = 1;
        i = 0;
        int $gensym23 = 8;
        if (i < $gensym23) {
            do {
                if ((s2 & mask << i) == 0) continue;
                result[i + 1 + 8] = 1;
            } while (++i < $gensym23);
        }
        mask = 1;
        i = 0;
        int $gensym24 = 8;
        if (i < $gensym24) {
            do {
                if ((s1 & mask << i) == 0) continue;
                result[i + 1 + 8 + 8] = 1;
            } while (++i < $gensym24);
        }
        return result;
    }

    private String toHex(byte[] buf) {
        String str = "";
        int i = 0;
        int $gensym25 = buf.length;
        if (i < $gensym25) {
            do {
                str = str + String.format("%02x ", new Byte(buf[i]));
            } while (++i < $gensym25);
        }
        return str;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private byte[] _send(byte[] request, int expect_response_size) {
        try {
            this.output.write(request);
            int error = 0;
            int readed = 0;
            byte[] response = new byte[expect_response_size];
            while (readed < expect_response_size) {
                int r = this.input.read(response, readed, expect_response_size - readed);
                if (r > 0) {
                    readed += r;
                    continue;
                }
                Thread.sleep(100);
                if (++error <= this.timeout) continue;
                return null;
            }
            System.out.println("send: " + this.toHex(request) + "\nrecv: " + this.toHex(response));
            byte[] arrby = response;
            return arrby;
        }
        catch (IOException iOException) {
            return null;
        }
        catch (InterruptedException interruptedException) {
            return null;
        }
    }
}
