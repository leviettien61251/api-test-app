# Testcase Row Status Colors

## Thay đổi

- Tô màu toàn bộ dòng testcase theo trạng thái thực thi trong bảng testcase.
- Trạng thái `PASS`: nền xanh lá cây.
- Trạng thái `FAIL`: nền đỏ.
- Trạng thái `Đang test...`: nền vàng.
- Trạng thái `Chờ`: nền xám.
- Tô màu nền từng dòng trong khu vực `KẾT QUẢ THỰC THI`.
- Log testcase, setup, cleanup và auth có `PASS` hoặc `✅`: nền xanh lá cây.
- Log có `FAIL`, `❌`, lỗi hoặc cảnh báo dừng thực thi: nền đỏ.
- Log trung tính giữ màu mặc định.
- Mỗi dòng log trong khu vực `KẾT QUẢ THỰC THI` có border để phân tách rõ ràng.
- Border mặc định màu xám; log thành công dùng border xanh và log lỗi dùng border đỏ.

## File đã sửa

- `src/main/java/com/example/apitestapp/controllers/TestcaseController.java`
  - Thêm row factory theo dõi property `result` của từng testcase.
  - Cập nhật pseudo-class của dòng ngay khi trạng thái testcase thay đổi.
  - Thêm cell factory phân loại màu nền cho từng dòng log kết quả thực thi.
- `src/main/resources/com/example/apitestapp/styles/styles.css`
  - Thêm style cho trạng thái của dòng testcase, log kết quả thực thi và border của từng dòng log.
- `src/main/resources/com/example/apitestapp/views/testcase-view.fxml`
  - Gắn style class riêng cho danh sách log để border không ảnh hưởng các `ListView` khác.
