package org.xjy.android.nova.transfer.upload.music;


import android.content.Context;

import org.xjy.android.nova.transfer.common.TransferDisposer;

public class UploadMusicMaster {
//    public static void uploadMusic(Context context, final LocalMusicInfo music) {
//        upload(context, new TransferDisposer() {
//            @Override
//            public void dispose() {
//                UploadMusicAgent.getInstance().upload(music);
//                PromptUtils.showToast(R.string.alreadyAddToUploadQueue);
//            }
//        });
//    }
//
//    public static void uploadMusics(Context context, final ArrayList<LocalMusicInfo> musics) {
//        upload(context, new TransferDisposer() {
//            @Override
//            public void dispose() {
//                UploadMusicAgent.getInstance().upload(musics);
//                PromptUtils.showToast(R.string.alreadyAddToUploadQueue);
//            }
//        });
//    }
//
//    public static void uploadEntry(Context context, final UploadMusicActivity.UploadMusicEntry entry) {
//        upload(context, new TransferDisposer() {
//            @Override
//            public void dispose() {
//                UploadMusicAgent.getInstance().uploadEntry(entry);
//            }
//        });
//    }
//
//    public static void uploadEntries(Context context, final ArrayList<UploadMusicActivity.UploadMusicEntry> entries) {
//        upload(context, new TransferDisposer() {
//            @Override
//            public void dispose() {
//                UploadMusicAgent.getInstance().uploadEntries(entries);
//            }
//        });
//    }

    public static void upload(Context context, final TransferDisposer disposer) {
//        int networkState = DeviceInfoUtils.getNetworkState();
//        if (networkState == DeviceInfoUtils.NETWORK_STATE_DISCONNECTED) {
//            PromptUtils.showToast(R.string.noNetwork);
//        } else if (networkState == DeviceInfoUtils.NETWORK_STATE_MOBILE) {
//            if (ifConnectOnlyInWifi()) {
//                PromptUtils.showWifiOnlyDialogDirectly(context, null, null);
//            } else if (!dataPackageAvailable()) {
//                MaterialDialogHelper.materialDialogWithPositiveBtn(context, R.string.uploadMusicPrompt, R.string.start, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        disposer.dispose();
//                    }
//                });
//            } else {
//                disposer.dispose();
//            }
//        } else {
//            disposer.dispose();
//        }
    }
}
