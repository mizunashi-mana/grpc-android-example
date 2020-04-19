package com.example.myapplication.mock

import com.example.myapplication.proto.Hello
import com.example.myapplication.proto.ServiceGrpcKt
import io.grpc.Server
import io.grpc.ServerBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class HelloServer constructor(
    private val port: Int
) {
    val server: Server = ServerBuilder
        .forPort(port)
        .addService(HelloService())
        .build()

    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@HelloServer.stop()
                println("*** server shut down")
            }
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

    private class HelloService: ServiceGrpcKt.ServiceCoroutineImplBase() {
        override fun connect(requests: Flow<Hello.Post>): Flow<Hello.Post> = flow {
            requests.collect { request ->
                emit(request)
            }
        }
    }
}

fun main() {
    val port = 5000
    val server = HelloServer(port)
    server.start()
    server.blockUntilShutdown()
}
