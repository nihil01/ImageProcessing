package world.horosho.prictureprocessor.imageProcessing.engine;

public enum ConvolutionPresets {

    GAUSSIAN_BLUR(new double[][]{
            { 1, 2, 1 },
            { 2, 4, 2 },
            { 1, 2, 1 }
    }),

    SHARPENING(new double[][]{
            { 0 , -2  , 0  },
            { -2,  1  ,-2  },
            { 0 , -2  , 0  }
    }),

    MEAN_REMOVAL(new double[][] {
            { -1 , -1, -1 },
            { -1 ,  9, -1 },
            { -1 , -1, -1 }
    }),

    EMBOSS(new double[][]{
            { -1 ,  0, -1 },
            {  0 ,  4,  0 },
            { -1 ,  0, -1 }
    });

    private final double[][] values;

    ConvolutionPresets(double[][] values){
        this.values = values;
    }


    public double[][] getValues() {
        return values;
    }

    public static double[][] modifyPresetWithValues(double[][] preset, int pos1, int pos2, double value){
        preset[pos1][pos2] = value;
        return preset;
    }
}
