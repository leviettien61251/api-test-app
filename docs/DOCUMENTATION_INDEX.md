# Chi muc tai lieu

Bo tai lieu nay duoc cap nhat theo code hien tai cua `API Test App`.

## Thu tu doc khuyen nghi

1. [README.md](../README.md)
2. [TAI_LIEU_HE_THONG.md](TAI_LIEU_HE_THONG.md)
3. [MA_TRAN_MAN_HINH.md](MA_TRAN_MAN_HINH.md)
4. [USECASE_TONG_QUAT.md](USECASE_TONG_QUAT.md)
5. [USECASE_PHAN_RA.md](USECASE_PHAN_RA.md)
6. [HUONG_DAN_SU_DUNG.md](HUONG_DAN_SU_DUNG.md)
7. [THIET_LAP_VA_VAN_HANH.md](THIET_LAP_VA_VAN_HANH.md)
8. [ARCHITECTURE.md](ARCHITECTURE.md)

## Vai tro tung tai lieu

- `README.md`: tong quan nhanh, stack, lenh build/run va link docs.
- `TAI_LIEU_HE_THONG.md`: pham vi he thong, actor, module, persistence, runtime config va gioi han.
- `MA_TRAN_MAN_HINH.md`: FXML/controller, trang thai navigation va ghi chu tung man hinh.
- `USECASE_TONG_QUAT.md`: actor, he thong ngoai va use case muc cao.
- `USECASE_PHAN_RA.md`: phan ra use case theo module UI/nghiep vu.
- `HUONG_DAN_SU_DUNG.md`: workflow thao tac cho tester.
- `THIET_LAP_VA_VAN_HANH.md`: yeu cau moi truong, DB, storage, troubleshooting.
- `ARCHITECTURE.md`: package layout, luong khoi dong, luong chay testcase va service/repository.

## Diem can luu y khi doc

- `database.sql` la file tham khao dang tron DDL, DML va cau truy van debug; khong nen xem la migration sach.
- Thu muc `src/main/resources/db/migrations` co cac migration bo sung cho `path_params`, `payload_assertions`, `expected_response_body`.
- `Request` hien da ap dung custom headers, Basic Auth va Bearer Token vao HTTP request.
- `Request` co form-data UI va multipart form-data cho method khong phai `GET`/`DELETE`.
- `Collections` va `Environments` ton tai trong repo nhung chua nam trong navigation chinh.
- `Default run config` hien chi cho nhap `Base URL` va `Alert mode`; runner duoc lay tu session username hoac system user.
