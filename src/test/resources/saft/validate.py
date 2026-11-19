"""Validate lab XML with the original XSD 1.1. Requires xmlschema 4.3.0."""
import hashlib
import pathlib
import sys
import xmlschema

schema = pathlib.Path(sys.argv[1])
assert hashlib.sha256(schema.read_bytes()).hexdigest() == (
    "292c0ab4611e3f5a0cdf2abb4e62d9bd41254dc2e76a1fae4d35a8b132d79350"
)
assert xmlschema.__version__ == "4.3.0"
xmlschema.XMLSchema11(schema, allow="none").validate(sys.stdin.buffer.read())
print("PASS: XSD 1.1, SAF-T (PT) 1.04_01")
