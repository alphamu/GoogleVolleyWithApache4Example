# Example Google Volley With Apache4 (4.4.1.1)

This is an example project done using the unofficial mirror of [Google's Volley project](https://android.googlesource.com/platform/frameworks/volley/) for Android.
The [unofficial mirror of Google volley uses Apache 4](https://github.com/alphamu/GoogleVolleyWithApache4) instead of the legacy Apache library bundled with Android.
Since the current master code contains references to the legacy apache library, I've update the code to use the an [apache httpclient port
for Android maintained by Marek Sebera](https://github.com/smarek/httpclient-android/wiki/Project-Introduction). Since this port 
is [mentioned on Apache's website](https://hc.apache.org/httpcomponents-client-4.5.x/android-port.html), I can only assume it's 
atleast recognized by the Apache Foundation.

You can take a look at the [Volley with Apache 4 project here](https://github.com/alphamu/GoogleVolleyWithApache4Example).

# License

Copyright (C) 2011 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
