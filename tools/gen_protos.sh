ROOT=$(cd "$(dirname "$0")/.."; pwd)
SRC_DIR=$ROOT/protos/src/main/proto
OUT_DIR=$ROOT/protos/src/main/java
rm -rf $OUT_DIR/*
protoc -I $SRC_DIR $SRC_DIR/* --java_out=$OUT_DIR
