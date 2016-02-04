package org.md2k.sensordataquality.dataquality.autosense.ecg;

import android.content.Context;

import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeInt;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.sensordataquality.Status;
import org.md2k.sensordataquality.dataquality.DataQuality;
import org.md2k.sensordataquality.dataquality.autosense.respiration.RIPQualityCalculation;

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
public class DataQualityECG extends DataQuality {
    ArrayList<Integer> samples;

    ECGQualityCalculation ecgQualityCalculation;

    public DataQualityECG(Context context, DataSource dataSource) {
        super(context, dataSource);
        samples=new ArrayList<>();
        ecgQualityCalculation = new ECGQualityCalculation();
    }

    @Override
    public void subscribe() throws Exception {
        ArrayList<DataSourceClient> dataSourceClientArrayList = dataKitAPI.find(new DataSourceBuilder(dataSource));
        if(dataSourceClientArrayList.size()!=0) throw new Exception("ERROR: No/Multiple datasource (AutoSense Chest-ECG)");
        dataSourceClient=dataSourceClientArrayList.get(0);
        dataKitAPI.subscribe(dataSourceClient, new OnReceiveListener() {
            @Override
            public void onReceived(DataType dataType) {
                DataTypeInt dataTypeInt = (DataTypeInt) dataType;
                samples.add(dataTypeInt.getSample());
            }
        });
    }

    @Override
    public void unsubscribe() {
        dataKitAPI.unregister(dataSourceClient);
    }
    @Override
    public Status getStatus() {
        if(samples.size()==0) return new Status(Status.OFF);
        return new Status(Status.GOOD);
    }

}
