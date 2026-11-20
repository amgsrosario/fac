"""Validate lab XML with the archived XSD 1.1, without external resources."""
import hashlib
import io
import json
import pathlib
import sys

import elementpath
import xmlschema

schema = pathlib.Path(sys.argv[1]).resolve(strict=True)
data = schema.read_bytes()
digest = hashlib.sha256(data).hexdigest()
if digest != "292c0ab4611e3f5a0cdf2abb4e62d9bd41254dc2e76a1fae4d35a8b132d79350":
    raise SystemExit("Unexpected SAF-T XSD SHA-256")
if xmlschema.__version__ != "4.3.0" or elementpath.__version__ != "5.1.1":
    raise SystemExit("Unexpected XSD engine version")
print(json.dumps({"xsd": str(schema), "sha256": digest, "python": sys.executable,
                  "python_version": sys.version, "xmlschema": xmlschema.__version__,
                  "elementpath": elementpath.__version__, "optimize": sys.flags.optimize}))
xmlschema.XMLSchema11(io.BytesIO(data), allow="none", defuse="always").validate(sys.stdin.buffer.read())
print("PASS: XSD 1.1, SAF-T (PT) 1.04_01")
