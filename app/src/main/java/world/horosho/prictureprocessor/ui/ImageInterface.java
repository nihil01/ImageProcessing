package world.horosho.prictureprocessor.ui;

import android.graphics.Bitmap;

public interface ImageInterface {
    void updateImage(Bitmap img);
    void modifySaveBtn(boolean state);
    void modifyGenerateBtn(boolean state);

}
