package org.anothermonitor;

import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class CpuStat {
    private static final String TAG = "CpuUsage";
    private RandomAccessFile statFile;
    private CpuInfo mCpuInfoTotal;
    private ArrayList<CpuInfo> mCpuInfoList;

    public CpuStat() {
    }

    public void update() {
        try {
            createFile();
            parseFile();
            closeFile();
        } catch (FileNotFoundException e) {
            statFile = null;
            Log.e(TAG, "cannot open /proc/stat: " + e);
        } catch (IOException e) {
            Log.e(TAG, "cannot close /proc/stat: " + e);
        }
    }

    private void createFile() throws FileNotFoundException {
        statFile = new RandomAccessFile("/proc/stat", "r");
    }

    public void closeFile() throws IOException {
        if (statFile != null)
            statFile.close();
    }

    private void parseFile() {
        if (statFile != null) {
            try {
                statFile.seek(0);
                String cpuLine = "";
                int cpuId = -1;
                do {
                    cpuLine = statFile.readLine();
                    parseCpuLine(cpuId, cpuLine);
                    cpuId++;
                } while (cpuLine != null);
            } catch (IOException e) {
                Log.e(TAG, "Ops: " + e);
            }
        }
    }

    private void parseCpuLine(int cpuId, String cpuLine) {
        if (cpuLine != null && cpuLine.length() > 0) {
            String[] parts = cpuLine.split("[ ]+");
            String cpuLabel = "cpu";
            if ( parts[0].indexOf(cpuLabel) != -1) {
                createCpuInfo(cpuId, parts);
            }
        }
        //else {
       //     Log.e(TAG, "unable to get cpu line");
       // }
    }

    private void createCpuInfo(int cpuId, String[] parts) {
        if (cpuId == -1) {
            if (mCpuInfoTotal == null)
                mCpuInfoTotal = new CpuInfo();
            mCpuInfoTotal.update(parts);
        } else {
            if (mCpuInfoList == null)
                mCpuInfoList = new ArrayList<CpuInfo>();
            if (cpuId < mCpuInfoList.size())
                mCpuInfoList.get(cpuId).update(parts);
            else {
                CpuInfo info = new CpuInfo();
                info.update(parts);
                mCpuInfoList.add(info);
            }
        }
    }

    public float getCpuUsage(int cpuId) {
        update();
        float usage = 0;
        if (mCpuInfoList != null) {
            int cpuCount = mCpuInfoList.size();
            if (cpuCount > 0) {
                cpuCount--;
                if (cpuId == cpuCount) { // -1 total cpu usage
                    usage = mCpuInfoList.get(0).getUsage();
                } else {
                    if (cpuId <= cpuCount)
                        usage = mCpuInfoList.get(cpuId).getUsage();
                    else
                        usage = -1;
                }
            }
        }
        return usage;
    }


    public float getTotalCpuUsage() {
       // update();
        float usage = 0;

        if (mCpuInfoList.size() > 0){
            for(int i = 0; i < mCpuInfoList.size(); i++)
                if(!Float.isNaN(mCpuInfoList.get(i).getUsage()))
                     usage += mCpuInfoList.get(i).getUsage();

            usage = usage / mCpuInfoList.size();
            }
        return usage;
    }

    public int getCpuCount(){
        return mCpuInfoList.size();
    }


    public String toString() {
      //  update();
        StringBuffer buf = new StringBuffer();

        if (mCpuInfoList != null) {
            for (int i=0; i < mCpuInfoList.size(); i++) {
                CpuInfo info = mCpuInfoList.get(i);
                buf.append("Core " + i + ": ");
                if(Float.isNaN(info.getUsage()))
                    buf.append("{:.2f}".format("%.2f", 0.00));
                else
                     buf.append("{:.2f}".format("%.2f", info.getUsage()));
                buf.append("%    ");
                info.getUsage();
            }
        }
        return buf.toString();
    }

    private float restrictPercentage(float percentage) {
        if (percentage > 100)
            return 100;
        else if (percentage < 0)
            return 0;
        else return percentage;
    }

    public class CpuInfo {
        private float mUsage;
        private long mLastTotal;
        private long mLastIdle;

        public CpuInfo() {
            mUsage = 0;
            mLastTotal = 0;
            mLastIdle = 0;
        }

        private float getUsage() {
            return restrictPercentage(mUsage);
        }

        public void update(String[] parts) {
            // the columns are:
            //
            //      0 "cpu": the string "cpu" that identifies the line
            //      1 user: normal processes executing in user mode
            //      2 nice: niced processes executing in user mode
            //      3 system: processes executing in kernel mode
            //      4 idle: twiddling thumbs
            //      5 iowait: waiting for I/O to complete
            //      6 irq: servicing interrupts
            //      7 softirq: servicing softirqs
            //
            long idle = Long.parseLong(parts[4], 10);
            long total = 0;
            boolean head = true;
            for (String part : parts) {
                if (head) {
                    head = false;
                    continue;
                }
                total += Long.parseLong(part, 10);
            }
            long diffIdle = idle - mLastIdle;
            long diffTotal = total - mLastTotal;
            mUsage =  ((float) (diffTotal - diffIdle) / diffTotal * 100);
            mLastTotal = total;
            mLastIdle = idle;
            //   Log.i(TAG, "CPU total=" + total + "; idle=" + idle + "; usage=" + mUsage);
        }
    }
}