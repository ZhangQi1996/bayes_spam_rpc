namespace java com.zq.rpc
namespace py rpc

typedef i16 short
typedef i32 int
typedef i64 long
typedef string String
typedef bool boolean
typedef binary bytes

struct RpcResult {
    1: optional short status_code; // 0表示成功，1表示内部出错
    2: optional short clazz;	// 0表示正常，1表示垃圾邮件
}

exception DataException {
    1: optional String message;
    2: optional String traceback;
    3: optional String date;
}

service MailQueryService {
    RpcResult queryMailClass(1: required bytes data)
        throws (1: DataException dataException);
}
