package com.vonchange.utao.gecko.impl;

import android.util.Log;

import androidx.annotation.NonNull;

import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.WebExtension;
import org.mozilla.geckoview.WebExtensionController;

public class GeckviewListener implements WebExtensionController.PromptDelegate {
    @Override
    public GeckoResult<AllowOrDeny> onInstallPrompt(final @NonNull WebExtension extension) {
        Log.i("onInstallPrompt","onInstallPrompt");
        return GeckoResult.fromValue(AllowOrDeny.ALLOW);
    }
}
