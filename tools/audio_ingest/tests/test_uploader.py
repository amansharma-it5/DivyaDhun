import tempfile
import unittest
from pathlib import Path
from types import SimpleNamespace
from unittest.mock import patch

from tools.audio_ingest.__main__ import _verify_url


class UploaderVerificationTest(unittest.TestCase):
    def test_verify_url_checks_mime_length_and_byte_range(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "track.mp3"
            path.write_bytes(b"abc")
            head = SimpleNamespace(status=200, headers={
                "Content-Type": "audio/mpeg",
                "Content-Length": "3",
                "Accept-Ranges": "bytes",
            })
            ranged = SimpleNamespace(status=206, headers={
                "Content-Length": "2",
                "Content-Range": "bytes 0-1/3",
            })
            with patch("tools.audio_ingest.__main__.urlopen", side_effect=[head, ranged]) as urlopen:
                _verify_url("https://audio.example/track.mp3", str(path))
            self.assertEqual(2, urlopen.call_count)

    def test_verify_url_fails_closed_on_wrong_content_length(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "track.mp3"
            path.write_bytes(b"abc")
            head = SimpleNamespace(status=200, headers={
                "Content-Type": "audio/mpeg",
                "Content-Length": "4",
                "Accept-Ranges": "bytes",
            })
            with patch("tools.audio_ingest.__main__.urlopen", return_value=head):
                with self.assertRaisesRegex(RuntimeError, "Content-Length"):
                    _verify_url("https://audio.example/track.mp3", str(path))


if __name__ == "__main__":
    unittest.main()
