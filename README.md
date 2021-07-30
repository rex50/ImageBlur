# ImageBlur

![Image Blur Library](images/Library-hero-poster.png)

An efficient yet easy to use image blurring utility library for Android made with Kotlin and Couroutines

Based on [BlurImage](https://github.com/sparrow007/BlurImage) library


## Gradle
#### Step 1. Add the dependency

```
dependencies {
    ...
    Implementation 'url'
}
```

#### Step 2. Add the below lines on app module build.gradle file.

```groovy
defaultConfig {
    ...
    renderscriptTargetApi 19
    renderscriptSupportModeEnabled true
}
```


## Usage

Just use as if you're using Glide.

### Directly apply blurred image to ImageView

```koltin
ImageBlur.with(this)
    .load(image) //Bitmap from which blurred image will be created
    .intensity(15.0f) //Blur intensity
    .scale(0.5f) //Scale intensity
    .into(binding?.ivPreview) //Directly apply to image view
```


Other options

### Get blurred bitmap

```koltin
CoroutineScope(Dispatchers.Main).launch {
    val blurredBitmap = ImageBlur.with(this)
                            .load(image)
                            .intensity(15.0f)
                            .scale(0.5f)

                            //There are 2 ways you can get a blurred bitmap

                            .getBlurredImage() //For getting blurred bitmap

                            or

                            .getBlurredImageAsync() //For getting blurred bitmap asynchronously
}
```

## Proguard

If you're using add this below lines to your app level proguard file

```
# RenderScript
-keepclasseswithmembernames class * {
native <methods>;
}
-keep class androidx.renderscript.** { *; }
```


## License 🔖

```
MIT License

Copyright (c) 2021 Pavitra R

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
