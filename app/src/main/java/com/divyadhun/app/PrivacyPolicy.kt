package com.divyadhun.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

const val PRIVACY_POLICY_URL = "https://amansharma-it5.github.io/divyadhun-privacy/"

fun privacyPolicyIntent(): Intent = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)).apply {
    addCategory(Intent.CATEGORY_BROWSABLE)
}

/** Opens the published policy without suppressing the launch because of package-visibility checks. */
fun openPrivacyPolicy(context: Context): Boolean = try {
    context.startActivity(privacyPolicyIntent())
    true
} catch (_: ActivityNotFoundException) {
    false
}
