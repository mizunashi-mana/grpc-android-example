package com.example.myapplication

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myapplication.proto.Hello
import com.example.myapplication.proto.ServiceGrpc
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var handler: Handler
    private lateinit var stream: StreamObserver<Hello.Post>
    private lateinit var binding: ActivityMainBinding

    private var i: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler = Handler()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        stream = startgRPCClient(handler, binding)

        binding.button.setOnClickListener {
            stream.onNext(
                Hello.Post.newBuilder()
                    .setMessage("button clicked: $i")
                    .build()
            )
            i++
        }
    }

    companion object {
        private fun startgRPCClient(
            mainHandler: Handler,
            binding: ActivityMainBinding
        ): StreamObserver<Hello.Post> {
            val channel = ManagedChannelBuilder
                .forAddress("10.0.2.2", 5000)
                .usePlaintext()
                .build()
            val stub = ServiceGrpc.newStub(channel)

            return stub.connect(object : StreamObserver<Hello.Post> {
                override fun onNext(post: Hello.Post) {
                    mainHandler.post {
                        binding.textView.text = post.message
                    }
                }

                override fun onError(t: Throwable) {
                    Log.e("app", "gRPC error", t)
                }

                override fun onCompleted() {
                    Log.i("app", "gRPC connection closed")
                }
            })
        }
    }
}
