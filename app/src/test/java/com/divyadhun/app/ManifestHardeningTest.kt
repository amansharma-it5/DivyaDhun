package com.divyadhun.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ManifestHardeningTest {
    @Test fun backupRulesExcludeLocalUserData() {
        val rules = File("src/main/res/xml/data_extraction_rules.xml").readText()
        assertTrue(rules.contains("<cloud-backup"))
        assertTrue(rules.contains("<device-transfer>"))
        assertTrue(rules.contains("domain=\"sharedpref\""))
    }
}
