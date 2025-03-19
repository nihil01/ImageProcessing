package world.horosho.prictureprocessor.notifications;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import world.horosho.prictureprocessor.R;

public class NotificationService extends Worker {
    private final Context ctx;

    public NotificationService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.ctx = context;
    }

    public static boolean checkNotificationPermission(Context ctx){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public void buildNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, "image_processor")
                .setSmallIcon(R.drawable.app_notification)
                .setContentTitle("Picture Processor")
                .setContentText("Hello! Time to get new stuff generated !1!1 ))")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat nmc = NotificationManagerCompat.from(ctx);

        if (NotificationService.checkNotificationPermission(ctx)){
            nmc.notify(1, builder.build());
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        this.buildNotification();
        return Result.success();
    }
}
