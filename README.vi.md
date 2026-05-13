# Đóng góp cá nhân

Repository này là bản public snapshot được clone từ repository private gốc của nhóm, vì vậy lịch sử commit hiện tại không phản ánh chính xác mức độ đóng góp của từng thành viên.

Tài liệu này tóm tắt phần đóng góp thực tế của mình dựa trên mã nguồn cuối cùng, báo cáo nhóm và phần công việc tích hợp mình trực tiếp tham gia trong quá trình phát triển.

## Bối cảnh dự án

Đây là ứng dụng Android hỗ trợ quản lý dự án và nhân sự, được xây dựng với:

- Java
- Android Studio
- Firebase Authentication
- Cloud Firestore
- Cloudinary

Các module chính của ứng dụng gồm xác thực tài khoản, quản lý nhân viên, quản lý dự án, quản lý task, chấm công, lương, chi phí, reminder, notification và settings.

## Vai trò chính của mình

Trong báo cáo nhóm, phần việc chính của mình được ghi nhận là **đăng nhập - đăng ký**. Tuy nhiên, trên thực tế mình còn tham gia khá nhiều ở phần **tích hợp hệ thống**, đặc biệt là các khu vực cần đồng bộ hành vi giao diện với dữ liệu thật trên Firebase/Firestore.

Phần đóng góp nổi bật nhất của mình tập trung ở:

- xây dựng luồng đăng nhập và đăng ký;
- kết nối phần xác thực với dữ liệu hồ sơ người dùng;
- hoàn thiện các màn hình liên quan đến tài khoản và luồng người dùng;
- hỗ trợ merge, rà soát và ổn định các màn hình dùng chung dữ liệu;
- cải thiện sự nhất quán giữa giao diện và dữ liệu lưu trong hệ thống.

## Các đóng góp chính

### 1. Luồng xác thực tài khoản

Mình là người phụ trách chính cho luồng tài khoản của ứng dụng:

- đăng nhập;
- đăng ký;
- đăng xuất;
- quên mật khẩu;
- đổi mật khẩu;
- xóa tài khoản;
- validate cơ bản cho các form tài khoản.

Các file liên quan:

- [`app/src/main/java/com/example/group13/activity/MainActivity.java`](app/src/main/java/com/example/group13/activity/MainActivity.java)
- [`app/src/main/java/com/example/group13/activity/SignupActivity.java`](app/src/main/java/com/example/group13/activity/SignupActivity.java)
- [`app/src/main/java/com/example/group13/activity/SettingActivity.java`](app/src/main/java/com/example/group13/activity/SettingActivity.java)
- [`app/src/main/java/com/example/group13/activity/SettingChangepasswordActivity.java`](app/src/main/java/com/example/group13/activity/SettingChangepasswordActivity.java)

### 2. Kết nối Authentication với hồ sơ người dùng

Ngoài phần màn hình đăng nhập và đăng ký, mình còn tham gia hoàn thiện luồng nối giữa Firebase Authentication và dữ liệu người dùng trong Firestore:

- tạo document `users` ban đầu sau khi đăng ký;
- sử dụng `profileCompleted` để điều hướng người dùng sang bước hoàn thiện hồ sơ;
- kết nối dữ liệu tài khoản với Home và Settings;
- đồng bộ các field hiển thị như `employeeName`, `employeeId`, `position`, `department`, `avatarUrl` với dữ liệu trong Firestore.

Các file liên quan:

- [`app/src/main/java/com/example/group13/activity/MainActivity.java`](app/src/main/java/com/example/group13/activity/MainActivity.java)
- [`app/src/main/java/com/example/group13/activity/SignupActivity.java`](app/src/main/java/com/example/group13/activity/SignupActivity.java)
- [`app/src/main/java/com/example/group13/activity/EmployeeAddEditActivity.java`](app/src/main/java/com/example/group13/activity/EmployeeAddEditActivity.java)
- [`app/src/main/java/com/example/group13/activity/HomeActivity.java`](app/src/main/java/com/example/group13/activity/HomeActivity.java)
- [`app/src/main/java/com/example/group13/activity/SettingActivity.java`](app/src/main/java/com/example/group13/activity/SettingActivity.java)

### 3. Đồng bộ giao diện với database

Đây là phần báo cáo chưa phản ánh hết, nhưng là phần mình tham gia khá rõ trong giai đoạn hoàn thiện sản phẩm.

Phần việc này chủ yếu xoay quanh:

- kiểm tra dữ liệu nhập từ form trước khi ghi lên Firestore;
- rà soát cách dùng field chung giữa nhiều màn hình;
- giảm lệch giữa UI, model và document trong database;
- hỗ trợ bảo đảm dữ liệu nhân viên, project, salary, cost và notification được hiển thị nhất quán giữa các màn hình.

Các khu vực liên quan:

- [`app/src/main/java/com/example/group13/activity/EmployeeAddEditActivity.java`](app/src/main/java/com/example/group13/activity/EmployeeAddEditActivity.java)
- [`app/src/main/java/com/example/group13/activity/ProjectAddEditActivity.java`](app/src/main/java/com/example/group13/activity/ProjectAddEditActivity.java)
- [`app/src/main/java/com/example/group13/activity/SalaryListActivity.java`](app/src/main/java/com/example/group13/activity/SalaryListActivity.java)
- [`app/src/main/java/com/example/group13/activity/SalaryAddEditActivity.java`](app/src/main/java/com/example/group13/activity/SalaryAddEditActivity.java)
- [`app/src/main/java/com/example/group13/activity/CostProjectListActivity.java`](app/src/main/java/com/example/group13/activity/CostProjectListActivity.java)
- [`app/src/main/java/com/example/group13/activity/CostActivity.java`](app/src/main/java/com/example/group13/activity/CostActivity.java)
- [`app/src/main/java/com/example/group13/activity/SettingNotificationActivity.java`](app/src/main/java/com/example/group13/activity/SettingNotificationActivity.java)

### 4. Tích hợp và ổn định sản phẩm ở giai đoạn cuối

Ở giai đoạn sau, mình cũng tham gia vào các phần việc thiên về tích hợp và hoàn thiện hệ thống:

- hỗ trợ merge code;
- xử lý lỗi giữa các module;
- làm mượt các luồng dùng chung;
- rà soát lại các màn hình sau khi ghép nhiều phần code;
- góp phần ổn định sản phẩm trước khi hoàn thiện đồ án.

Các file thể hiện rõ phần luồng dùng chung:

- [`app/src/main/java/com/example/group13/base/BaseActivity.java`](app/src/main/java/com/example/group13/base/BaseActivity.java)
- [`app/src/main/java/com/example/group13/activity/HomeActivity.java`](app/src/main/java/com/example/group13/activity/HomeActivity.java)
- [`app/src/main/java/com/example/group13/activity/SettingActivity.java`](app/src/main/java/com/example/group13/activity/SettingActivity.java)
- [`app/src/main/java/com/example/group13/activity/SettingNotificationActivity.java`](app/src/main/java/com/example/group13/activity/SettingNotificationActivity.java)

## Điều README này muốn làm rõ

Nếu chỉ nhìn vào báo cáo nhóm, phần việc của mình có thể bị hiểu là chỉ xoay quanh đăng nhập và đăng ký. Nhưng trong quá trình triển khai thực tế, mình còn đóng góp thêm ở các phần:

- tích hợp luồng tài khoản và hồ sơ người dùng;
- đồng bộ giao diện với Firebase và Firestore;
- rà soát tính nhất quán giữa các màn hình dùng chung cấu trúc dữ liệu;
- hỗ trợ fix lỗi và ổn định hệ thống sau khi merge nhiều module.

Nói ngắn gọn, đóng góp của mình không chỉ nằm ở **authentication**, mà còn ở phần **integration** và **data consistency** của ứng dụng.
