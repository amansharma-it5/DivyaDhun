import hashlib
import json
import tempfile
import unittest
from pathlib import Path

from tools.audio_ingest.pipeline import (
    build_audio_url,
    build_manifest,
    build_upload_plan,
    detect_duplicates,
    scan_directory,
    sha256_file,
    slugify,
    validate_manifest,
)


class PipelineTest(unittest.TestCase):
    def test_slugging_is_deterministic_and_safe(self):
        self.assertEqual("hanuman-chalisa-mp3", slugify("Hanuman  Chalisa!.mp3"))
        self.assertEqual("untitled", slugify(" हिन्दी "))
        self.assertNotRegex(slugify("A\\B:C"), r"[\\/: ]")

    def test_hashing(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "sample.wav"
            path.write_bytes(b"divyadhun")
            self.assertEqual(hashlib.sha256(b"divyadhun").hexdigest(), sha256_file(path))

    def test_scan_ignores_unrelated_files_and_does_not_modify_inputs(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "Hanuman Chalisa.mp3").write_bytes(b"fixture")
            (root / "notes.txt").write_text("ignore", encoding="utf-8")
            records = scan_directory(root, ffprobe="missing-ffprobe")
            self.assertEqual(1, len(records))
            self.assertEqual("Hanuman Chalisa.mp3", records[0]["originalFilename"])
            self.assertEqual(b"fixture", (root / "Hanuman Chalisa.mp3").read_bytes())
            self.assertEqual("hanuman", records[0]["deityId"])
            self.assertEqual("CHALISA", records[0]["category"])

    def test_duplicate_detection_reports_exact_and_potential_matches(self):
        rows = [
            {"relativePath": "a.mp3", "sha256": "same", "metadataTitle": "Prayer"},
            {"relativePath": "b.mp3", "sha256": "same", "metadataTitle": "Prayer"},
        ]
        result = detect_duplicates(rows)
        self.assertEqual([["a.mp3", "b.mp3"]], result["exactSha256"])
        self.assertEqual([["a.mp3", "b.mp3"]], result["potentialTitle"])

    def test_url_building_requires_https_and_supports_cdn_replacement(self):
        key = "audio/hanuman/hanuman-chalisa.m4a"
        self.assertEqual("https://pub.example/audio/hanuman/hanuman-chalisa.m4a", build_audio_url("https://pub.example/", key))
        self.assertEqual("https://audio.divyadhun.com/audio/hanuman/hanuman-chalisa.m4a", build_audio_url("https://audio.divyadhun.com", key))
        with self.assertRaises(ValueError):
            build_audio_url("http://insecure.example", key)

    def test_manifest_validation_and_rights_gate(self):
        manifest = build_manifest([{
            "originalFilename": "Hanuman Chalisa.mp3",
            "extension": ".mp3",
            "metadataTitle": "Hanuman Chalisa",
            "metadataArtist": "",
            "deityId": "hanuman",
            "category": "CHALISA",
            "objectKey": "audio/hanuman/hanuman-chalisa.mp3",
            "durationMs": 1234,
            "sha256": "abc",
            "valid": True,
            "needsReview": True,
        }], "https://audio.divyadhun.com")
        result = validate_manifest(manifest)
        self.assertFalse(result["errors"])
        self.assertTrue(result["warnings"])
        self.assertEqual("UNKNOWN", manifest["tracks"][0]["rightsStatus"])
        with tempfile.TemporaryDirectory() as directory:
            plan = build_upload_plan(manifest, directory)
            self.assertEqual("BLOCKED_RIGHTS", plan[0]["status"])

    def test_validator_rejects_http_and_duplicate_ids(self):
        manifest = {"tracks": [
            {"id": "same", "titleEnglish": "A", "deityId": "hanuman", "category": "AARTI", "rightsStatus": "LICENSED", "audioObjectKey": "a.mp3", "audioUrl": "http://bad"},
            {"id": "same", "titleEnglish": "B", "deityId": "hanuman", "category": "AARTI", "rightsStatus": "LICENSED", "audioObjectKey": "a.mp3", "audioUrl": "https://ok"},
        ]}
        result = validate_manifest(manifest)
        self.assertTrue(any("duplicate track id" in error for error in result["errors"]))
        self.assertTrue(any("HTTPS" in error for error in result["errors"]))
        self.assertTrue(any("duplicate object key" in error for error in result["errors"]))

    def test_validator_rejects_unsafe_object_key(self):
        manifest = {"tracks": [{
            "id": "safe",
            "titleEnglish": "A",
            "deityId": "hanuman",
            "category": "AARTI",
            "rightsStatus": "UNKNOWN",
            "audioObjectKey": "audio/../outside.mp3",
        }]}
        result = validate_manifest(manifest)
        self.assertTrue(any("unsafe audio object key" in error for error in result["errors"]))


if __name__ == "__main__":
    unittest.main()
