package world.horosho.prictureprocessor.imageProcessing.executor;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import world.horosho.prictureprocessor.ui.ImageInterface;

public class AsyncExecutor {

    private final ExecutorService executor;
    private final Handler handler;
    private Context ctx;
    private ImageInterface ii;

    public AsyncExecutor(Context ctx, ImageInterface updateListener){
        this.ii = updateListener;
        this.ctx = ctx;
        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
    }

    public void exec(Supplier<Bitmap> res){
        Toast.makeText(ctx, "This operation should take for a while... Please wait", Toast.LENGTH_LONG).show();
        ii.modifySaveBtn(false);
        ii.modifyGenerateBtn(false);

        executor.execute(() -> {

            Bitmap result = res.get();

            handler.post(() -> {
                if (result != null) {
                    ii.updateImage(result);
                }
                ii.modifyGenerateBtn(true);
                ii.modifySaveBtn(true);
                Toast.makeText(ctx, "Your image is ready!", Toast.LENGTH_SHORT).show();
            });

        });

    }

}
