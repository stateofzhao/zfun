// Created by Xcar on 2015/11/20


/* 包含的文件（相当于java中的倒包）begin */
#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/inotify.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>

#include <android/log.h>
#include <string.h>

/* 包含的文件 end */


/* LOG宏定义 */
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
/* 宏定义end */

static char TAG[] = "ObserverUninstall.init";
static jboolean isCopy = JNI_TRUE;

//app在手机内部存储数据的目录
static const char APP_DIR[] = "/data/data/com.xcar.activity";
//存储目录下的files路径
static const char APP_FILES_DIR[] = "/data/data/com.xcar.activity/files";
//用来检测这个文件是否改变了，
static const char APP_OBSERVED_FILE[] = "/data/data/com.xcar.activity/files/observedFile";
//用来判断是否已经有子进程锁定了此文件，防止多次调用init()方法导致多次开启子进程，
static const char APP_LOCK_FILE[] = "/data/data/com.xcar.activity/files/lockFile";

static char *hostName;
static char *hostFile;
static int hasUpdateHostFile=-1;


/* hostName 就是域名 www.baidu.com*/
void ndkRequestHttp() {

    int portnumber = 80;
    char host_addr[256];
    char host_file[1024];

    char request[1024];

    strcpy(host_addr, hostName);
    strcpy(host_file, hostFile);

    LOGI("ndkRequestHttp HostName %s", hostName);
    LOGI("ndkRequestHttp hostFile %s", hostFile);
//    LOGD("进入ndk请求http的方法,hostName：%s；url：%s", host_addr, host_file);

    //通过域名查询主机ip等信息
    struct hostent *host;
    if ((host = gethostbyname(host_addr)) == NULL)/*取得主机IP地址*/
    {
//        LOGD("Gethostname 出错 ... %s\n", strerror(errno));
        return;
    }

    int socket_handle = socket(AF_INET, SOCK_STREAM, 0);

    if (socket_handle < 0) {
//        LOGD("建立socket()出错 ... %s", strerror(errno));
        return;
    } else {
//        LOGD("建立socket()成功 ... ");
        struct sockaddr_in loc_addr;//本机地址
        loc_addr.sin_family = AF_INET;//协议,IPV4
        loc_addr.sin_addr.s_addr = htons(INADDR_ANY);
        loc_addr.sin_port = htons(INADDR_ANY);

        if (bind(socket_handle, &loc_addr, sizeof(struct sockaddr_in)) < 0) {
//            LOGD("bind()出错 ... %s", strerror(errno));
            close(socket_handle);
            return;
        } else {
//            LOGD("bind()成功 ... ");

            //服务器地址
            struct sockaddr_in serv_add;

            serv_add.sin_family = AF_INET;//协议,IPV4
            serv_add.sin_port = htons(portnumber);
            serv_add.sin_addr = *((struct in_addr *) host->h_addr);

            if (connect(socket_handle, &serv_add, sizeof(struct sockaddr_in)) < 0) {
//                LOGD("connect()出错 ... %s", strerror(errno));
                close(socket_handle);
                return;
            } else {

//                LOGD("connect()成功 ... ");

                //要发送的头部信息
                sprintf(request,
                        "GET /%s HTTP/1.1\r\nAccept: */*\r\nAccept-Language: zh-CN\r\nUser-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Linux)\r\nHost: %s:%d\r\nConnection: Close\r\n\r\n",
                        host_file, host_addr, portnumber);

                if (request == NULL) {
                    close(socket_handle);
                    return;
                }

                //request中有换行，换行后logcat只会显示最后一行的输出，由于request的结尾是\r\n\r\n 所以这里什么都不会显示
//                LOGI("要发送的request：%d", strlen(request));

                if (send(socket_handle, request, strlen(request), 0) < 0) {//发送头部信息
//                    LOGD("send()出错 ... %s", strerror(errno));
                    close(socket_handle);
                    return;
                } else {
//                    LOGD("send()成功 ... ");

                    char *result = (char *) malloc(sizeof(char));
                    char *temp_result = (char *) malloc(sizeof(char));
                    int SIZE = sizeof(char) * 1024;
                    char *cache = (char *) malloc(SIZE);
                    int len = 0;

                    memset(result, 0x00, sizeof(char));
                    memset(temp_result, 0x00, sizeof(char));
                    memset(cache, 0x00, SIZE);

                    len = recv(socket_handle, cache, SIZE, 0);//读服务器信息
                    int tempLen = sizeof(char) * strlen(result) + 1;
                    free(temp_result);
                    temp_result = (char *) malloc(tempLen);
                    memset(temp_result, 0x00, tempLen);
                    strcpy(temp_result, result);

                    free(result);
                    tempLen += strlen(cache);
                    result = (char *) malloc(tempLen);
                    memset(result, 0x00, tempLen);
                    strcpy(result, temp_result);
                    strcat(result, cache);
                    memset(cache, 0x00, SIZE);

                    LOGD("Uninstall result: ... %s s", result);
                    if (result != NULL)
                        free(result);
                    if (temp_result != NULL)
                        free(temp_result);
                    if (cache != NULL)
                        free(cache);

                }
            }
        }
    }

    close(socket_handle);

}

/*
 *Class:     com_diagramsj_test_jniclass_ObserverUninstall
 *Method:    init
 *
 * return: 子进程的pid
 * */
JNIEXPORT jint JNICALL Java_com_diagramsj_test_jniclass_ObserverUninstall_init
        (JNIEnv *env, jobject obj, jstring userSerial, jstring url, jstring host) {

//    LOGD("init observer初始化卸载监听");

    // fork子进程，以执行轮询任务
    pid_t pid = fork();
    if (pid < 0) {
//        LOGE("fork failed(子进程) !!!");
        exit(1);//非正常退出
    } else if (pid == 0) {//此时代码运行在子进程中
        // 打开监听的文件目录
        FILE *p_filesDir = fopen(APP_FILES_DIR, "r");
        if (p_filesDir == NULL) {//如果监听的文件目录不存在，就进行创建
            int filesDirRet = mkdir(APP_FILES_DIR, S_IRWXU | S_IRWXG | S_IXOTH);//这个方法是创建目录
            if (filesDirRet == -1) {
//                LOGE("mkdir failed (创建APP_FILES_DIR失败)!!!");
                exit(1);//非正常退出
            }
        }

        //=================================我把这块代码从下面移动到这里了，这样能够减少一次对文件的监听
        // 创建锁文件，通过检测加锁状态来保证只有一个卸载监听进程
        int lockFileDescriptor = open(APP_LOCK_FILE, O_RDONLY);
        if (-1 == lockFileDescriptor) {//如果文件不存在
            lockFileDescriptor = open(APP_LOCK_FILE, O_CREAT,0666);//创建文件
        }

        int lockRet = flock(lockFileDescriptor, LOCK_EX | LOCK_NB);
//        LOGD("检测是否有其他线程锁住了文件： %d", lockRet);
        if (-1 == lockRet) {
//            LOGD("observed by another process(另一个子进程已经在进行监听了，退出)");
            exit(0);//正常退出
        }
        //=============================================================================

        // 若被监听文件不存在，创建文件
        FILE *p_observedFile = fopen(APP_OBSERVED_FILE, "r");
        if (p_observedFile == NULL) {
            p_observedFile = fopen(APP_OBSERVED_FILE, "w");//这个方法是创建文件
        }
        fclose(p_observedFile);

        //open和fopen的区别：
        //前者属于低级IO，后者是高级IO。
        //前者返回一个文件描述符，后者返回一个文件指针。
        //前者无缓冲，后者有缓冲。
        //前者与 read, write 等配合使用， 后者与 fread, fwrite等配合使用。
        //后者是在前者的基础上扩充而来的，在大多数情况下，用后者。

        // 创建锁文件，通过检测加锁状态来保证只有一个卸载监听进程
        //int lockFileDescriptor = open(APP_LOCK_FILE, O_RDONLY);
        //if (-1 == lockFileDescriptor) {//如果文件不存在
        //lockFileDescriptor = open(APP_LOCK_FILE, O_CREAT);//创建文件
        //}
        //int lockRet = flock(lockFileDescriptor, LOCK_EX | LOCK_NB);
        //LOGD("检测是否有其他线程锁住了文件： %d",lockRet);
        //if (-1 == lockRet) {
        //LOGD("observed by another process(另一个子进程已经在进行监听了，退出)");
        //exit(0);//正常退出
        //}

//        LOGD("observed by childprocess(子进程开始进行监听文件变化)");

        // 分配空间，以便读取event
        void *p_buf = malloc(sizeof(struct inotify_event));
        if (p_buf == NULL) {
//            LOGE("malloc failed !!!(分配空间失败)");
            exit(1);
        }
        // 分配空间，以便打印mask
        size_t maskStrLength = 7 + 10 + 1;// mask=0x占7字节，32位整形数最大为10位，转换为字符串占10字节，'\0'占1字节
        char *p_maskStr = malloc(maskStrLength);
        if (p_maskStr == NULL) {
            free(p_buf);
//            LOGE("malloc failed !!!(分配空间失败)");

            exit(1);
        }
        // 开始监听
//        LOGD("start observe(开始监听~)");

        // 初始化
        int fileDescriptor = inotify_init();
        if (fileDescriptor < 0) {
            free(p_buf);
            free(p_maskStr);
//            LOGE("inotify_init failed !!!(初始化linux文件监听失败)");

            exit(1);
        }
        // 添加被监听文件到监听列表
        int watchDescriptor = inotify_add_watch(fileDescriptor, APP_OBSERVED_FILE, IN_ALL_EVENTS);
        if (watchDescriptor < 0) {
            free(p_buf);
            free(p_maskStr);
//            LOGE("inotify_add_watch failed !!!(注册linux文件监听列表失败)");

            exit(1);
        }

        while (JNI_TRUE) {//开始无限循环,1是非0值，相当于while(JNI_TRUE)
            // read会阻塞进程
            size_t readBytes = read(fileDescriptor, p_buf, sizeof(struct inotify_event));
            // 打印mask
            snprintf(p_maskStr, maskStrLength, "mask=0x%x\0", ((struct inotify_event *) p_buf)->mask);
//            LOGD("在无限循环中执行开始===文件操作事件编码：%d", ((struct inotify_event *) p_buf)->mask);
            // 若文件被删除，可能是已卸载，还需进一步判断app文件夹是否存在；
            if (IN_DELETE_SELF == ((struct inotify_event *) p_buf)->mask ||
                IN_ATTRIB ==
                ((struct inotify_event *) p_buf)->mask) {//之前的代码是没有 IN_ATTRIB这个判断的，这里必须加上，因为用户手动清除数据时会接受到这个事件
//                LOGD("文件状态改变符合条件判断");
                sleep(1);//不知道为啥，有时候卸载app时会不删除 com.xx.xx 包名的情况下执行到这里,单位是 秒
//                LOGD("sleep完了");
                FILE *p_appDir = fopen(APP_DIR, "r");
                //               LOGD("以只读的方式获取app目录");
                // 确认已卸载
                if (p_appDir == NULL) {
                    inotify_rm_watch(fileDescriptor, watchDescriptor);
//                    LOGD("确认卸载了，下面跳出循环");
                    break;
                } else { // 未卸载，可能用户执行了"清除数据"
                    fclose(p_appDir);
                    //============下面的代码必须有，之前是没有的，如果没有会导致执行 inotify_add_watch()时出现异常子进程直接崩溃天退出
                    //导致用户清除数据后，再卸载无法监听到。
                    // 打开监听的文件目录
                    FILE *p_filesDir = fopen(APP_FILES_DIR, "r");
                    if (p_filesDir == NULL) {//如果监听的文件目录不存在，就进行创建
                        int filesDirRet = mkdir(APP_FILES_DIR, S_IRWXU | S_IRWXG | S_IXOTH);//这个方法是创建目录
                        if (filesDirRet == -1) {
//                            LOGE("mkdir failed (创建APP_FILES_DIR失败)!!!");
                            exit(1);//非正常退出
                        }
                    }
                    //====================================================================================


                    //=============下面的代码是我添加的，之前是没有的，由于清除数据会造成标记为锁的文件也会被删除，从而导致上面添加的文件锁失效，
                    //所以这里需要重新创建文件锁，防止重复创建子进程
                    int lockFileDescriptor = open(APP_LOCK_FILE, O_RDONLY);
                    if (-1 == lockFileDescriptor) {//如果文件不存在
                        lockFileDescriptor = open(APP_LOCK_FILE, O_CREAT,0666);//创建文件

                        lockRet = flock(lockFileDescriptor, LOCK_EX | LOCK_NB);
//                        LOGD("用户清除数据后，重新创建文件锁:检测是否有其他线程锁住了文件： %d", lockRet);
                        if (-1 == lockRet) {//失败后不用处理，只要文件锁存在即可。
//                            LOGD("用户清除数据后，重新创建文件锁失败");
                        } else {
//                            LOGD("用户清除数据后，重新创建文件锁成功");
                        }
                    }
                    //====================================================================================


                    // 重新创建被监听文件，并重新监听
                    // 若被监听文件不存在，创建文件
                    FILE *p_observedFile = fopen(APP_OBSERVED_FILE, "r");
                    if (p_observedFile == NULL) {
                        //去掉之前的监听，下面你会重新添加
                        inotify_rm_watch(fileDescriptor, watchDescriptor);
                        p_observedFile = fopen(APP_OBSERVED_FILE, "w");//这个方法是创建文件
//                        LOGD("未卸载，是用户清除了数据，重新创建监听文件成功");
                    }
                    fclose(p_observedFile);

                    //重新添加监听
                    watchDescriptor = inotify_add_watch(fileDescriptor, APP_OBSERVED_FILE, IN_ALL_EVENTS);

                    //                   LOGD("未卸载，清除了数据，重新添加监听列表");
                    if (watchDescriptor < 0) {
                        free(p_buf);
                        free(p_maskStr);
//                        LOGE("inotify_add_watch failed !!!(用户执行了清除数据后重新创建监听列表失败)");
                        exit(1);

                    }
//                    LOGD("用户清除数据，重新创建监听成功");
                }
            }
//            LOGD("接收到的文件操作事件码：%d", ((struct inotify_event *) p_buf)->mask);
//            LOGD("IN_DELETE_SELF文件操作事件码：%d", IN_DELETE_SELF);
//            LOGD("IN_ATTRIB文件操作事件码：%d", IN_ATTRIB);//当用户选择手动清除数据时，会接收到IN_ATTRIB这个事件
        }//while end

        // 释放资源
        free(p_buf);
        free(p_maskStr);
        // 停止监听
//        LOGD("stop observe(停止卸载监听)");

        if(hasUpdateHostFile == -1){
            hostName = (char *) (*env)->GetStringUTFChars(env, host, &isCopy);
            hostFile = (char *) (*env)->GetStringUTFChars(env, url, &isCopy);
            LOGI("hasUpdateHostFile = -1 %s", hostFile);
        }

        ndkRequestHttp();

//        if (userSerial == NULL) {
//            // 执行命令am start -a android.intent.action.VIEW -d $(url)
//            execlp("am", "am", "start", "-a", "android.intent.action.VIEW", "-d", "http://www.baidu.com",
//                   (char *) NULL);
//        } else {
//            // 执行命令am start --user userSerial -a android.intent.action.VIEW -d $(url)
//            execlp("am", "am", "start", "--user", (*env)->GetStringUTFChars(env, userSerial, &isCopy),
//                   "-a", "android.intent.action.VIEW", "-d", "http://www.baidu.com", (char *) NULL);
//        }

        exit(0);

        // 执行命令失败log
        //       LOGE("exec AM command failed!!!(执行android打开网页失败) ");

    } else {//此时代码运行在父进程中
        // 父进程直接退出，使子进程被init进程领养，以避免子进程僵死，同时返回子进程pid
        return pid;
    }

}

JNIEXPORT jint JNICALL Java_com_diagramsj_test_jniclass_ObserverUninstall_updateData
        (JNIEnv *env, jobject obj, jstring userSerial, jstring url, jstring host) {
    hostName = (char *) (*env)->GetStringUTFChars(env, host, &isCopy);
    hostFile = (char *) (*env)->GetStringUTFChars(env, url, &isCopy);
    LOGI("update HostName %s", hostName);
    LOGI("update hostFile %s", hostFile);
    hasUpdateHostFile = 1;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    void *venv;
    LOGI("JNI_OnLoad() %s","dufresne----->JNI_OnLoad!");
    if ((*vm)->GetEnv(vm, (void **) &venv, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("JNI_OnLoad() %s","dufresne--->ERROR: GetEnv failed");
        return -1;
    }
    return JNI_VERSION_1_4;
}
