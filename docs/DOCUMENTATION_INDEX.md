# Chi muc tai lieu

Bo docs hien tai da duoc dong bo lai theo code trong repo va duoc chia thanh 3 lop:

- tong quan san pham
- van hanh / su dung
- phan tich nghiep vu / ky thuat

## Thu tu doc khuyen nghi

1. [README.md](/D:/code/api-test-app/README.md)
2. [TAI_LIEU_HE_THONG.md](/D:/code/api-test-app/docs/TAI_LIEU_HE_THONG.md)
3. [MA_TRAN_MAN_HINH.md](/D:/code/api-test-app/docs/MA_TRAN_MAN_HINH.md)
4. [USECASE_TONG_QUAT.md](/D:/code/api-test-app/docs/USECASE_TONG_QUAT.md)
5. [USECASE_PHAN_RA.md](/D:/code/api-test-app/docs/USECASE_PHAN_RA.md)
6. [THIET_LAP_VA_VAN_HANH.md](/D:/code/api-test-app/docs/THIET_LAP_VA_VAN_HANH.md)
7. [HUONG_DAN_SU_DUNG.md](/D:/code/api-test-app/docs/HUONG_DAN_SU_DUNG.md)
8. [ARCHITECTURE.md](/D:/code/api-test-app/docs/ARCHITECTURE.md)

## Mo ta tung tai lieu

- `README.md`
  - tong quan nhanh
  - stack
  - lenh build/run
  - danh sach tai lieu

- `TAI_LIEU_HE_THONG.md`
  - pham vi san pham
  - thanh phan chinh
  - nguon testcase
  - persistence va runtime state
  - gioi han hien tai

- `MA_TRAN_MAN_HINH.md`
  - danh sach view/controller
  - man hinh nao dang hoat dong trong navigation chinh
  - man hinh nao moi o muc UI scaffold

- `USECASE_TONG_QUAT.md`
  - actor
  - so do use case muc cao
  - danh sach use case tong quat

- `USECASE_PHAN_RA.md`
  - phan ra use case theo module
  - cay use case chi tiet hon

- `THIET_LAP_VA_VAN_HANH.md`
  - cai dat JDK, Maven, PostgreSQL
  - chay project
  - khoi tao schema
  - xu ly su co hay gap

- `HUONG_DAN_SU_DUNG.md`
  - workflow su dung app theo vai tro tester
  - thao tac testcase, request, report, history

- `ARCHITECTURE.md`
  - package layout
  - luong dieu huong UI
  - luong chay testcase
  - service/repository/storage

## Ghi chu quan trong

- File schema [database.sql](/D:/code/api-test-app/src/main/resources/db/database.sql) khong phai migration sach; trong file dang tron `insert`, `select`, `drop`, `create`.
- UI auth trong `Request` da co, nhung logic gui request hien tai chua apply Basic/Bearer vao request.
- `Collections` va `Environments` ton tai trong repo nhung chua nam trong menu dieu huong chinh.
