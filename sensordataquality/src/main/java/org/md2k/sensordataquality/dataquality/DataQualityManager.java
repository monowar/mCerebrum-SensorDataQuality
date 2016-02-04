package org.md2k.sensordataquality.dataquality;

import android.content.Context;
import android.os.Handler;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.PlatformType;
import org.md2k.sensordataquality.Constants;
import org.md2k.sensordataquality.Status;
import org.md2k.sensordataquality.dataquality.autosense.ecg.DataQualityECG;
import org.md2k.sensordataquality.dataquality.autosense.respiration.DataQualityRIP;
import org.md2k.sensordataquality.dataquality.microsoftband.DataQualityMicrosoftBand;
import org.md2k.utilities.Files;
import org.md2k.utilities.Report.Log;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p/>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class DataQualityManager {
    private static final String TAG = DataQualityManager.class.getSimpleName();
    Context context;
    DataKitAPI dataKitAPI;
    ArrayList<DataSource> dataSources;
    ArrayList<DataQuality> dataQualities;
    Handler handler;

    public DataQualityManager(Context context) throws FileNotFoundException {
        this.context = context;
        dataKitAPI = DataKitAPI.getInstance(context);
        prepareDataSources();
        handler=new Handler();
    }

    void prepareDataSources() throws FileNotFoundException {
        dataSources=Files.readJSONArray(Constants.CONFIG_DIRECTORY, Constants.DEFAULT_CONFIG_FILENAME, DataSource.class);
        dataQualities=new ArrayList<>();
        for(int i=0;i<dataSources.size();i++){
            if(dataSources.get(i).getType().equals(DataSourceType.RESPIRATION) && dataSources.get(i).getPlatform().getType().equals(PlatformType.AUTOSENSE_CHEST))
                dataQualities.add(new DataQualityRIP(context, dataSources.get(i)));
            else if(dataSources.get(i).getType().equals(DataSourceType.ECG) && dataSources.get(i).getPlatform().getType().equals(PlatformType.AUTOSENSE_CHEST))
                dataQualities.add(new DataQualityECG(context, dataSources.get(i)));
            else if(dataSources.get(i).getPlatform().getType().equals(PlatformType.MICROSOFT_BAND))
                dataQualities.add(new DataQualityMicrosoftBand(context,dataSources.get(i)));
        }
    }

    public void start() throws Exception {
        for(int i=0;i<dataQualities.size();i++){
            dataQualities.get(i).subscribe();
        }
        handler.post(checkStatus);

    }
    public void stop() {
        handler.removeCallbacks(checkStatus);
        for(int i=0;i<dataQualities.size();i++){
            dataQualities.get(i).unsubscribe();
        }

    }
    private Runnable checkStatus=new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "status");
            for(int i=0;i<dataQualities.size();i++) {
                Status status = dataQualities.get(i).getStatus();
            }
            handler.postDelayed(checkStatus,5000);
        }
    };
}
