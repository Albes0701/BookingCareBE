--
-- PostgreSQL database dump
--

-- \restrict UcS09u3whoeENUV7V7ywSJ1CbAivmMUfHO2ShYRB22LgEC3OOaeFwExFAnwSKlW

-- Dumped from database version 18.0
-- Dumped by pg_dump version 18.0

-- Started on 2025-11-13 11:19:50

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 4924 (class 0 OID 16826)
-- Dependencies: 221
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.roles VALUES ('role-pat-1', 'PATIENT', false);
INSERT INTO public.roles VALUES ('role-doc-1', 'DOCTOR', false);
INSERT INTO public.roles VALUES ('role-adm-1', 'ADMIN', false);
INSERT INTO public.roles VALUES ('role-clinic-adm-1', 'CLINIC_ADMIN', false);







-- Users with role: DOCTOR
INSERT INTO public.users VALUES ('d4183f14-9953-4401-b2d9-b08c3f72cfcd', 'Tran Thi B', '1985-11-20', 'FEMALE', '0917654321', 'tranthib@hospital.com', '456 Hai Ba Trung, Hanoi', 'profile-b.jpg', FALSE);
INSERT INTO public.users VALUES ('6d3d7b15-33e8-4088-9026-f5aa8ea99eba', 'Le Minh Tuan', '1982-03-10', 'MALE', '0905001001', 'leminhtuan@cityhospital.com', '22 Nguyen Trai, District 1, HCMC', 'doctor-le-minh-tuan.jpg', FALSE);
INSERT INTO public.users VALUES ('bcfcd0af-2459-4a1f-b038-247a3d48a572', 'Pham Thi Hoa', '1979-08-22', 'FEMALE', '0905111222', 'phamthihua@centralhospital.vn', '18C Hai Ba Trung, Hanoi', 'doctor-pham-thi-hoa.jpg', FALSE);
INSERT INTO public.users VALUES ('c79e85ce-5e2b-49e5-8bf0-5d2529706bea', 'Nguyen Huu Khang', '1988-12-01', 'MALE', '0907788003', 'nguyenhuukhang@medicare.vn', '15 Hoang Dieu, Da Nang', 'doctor-nguyen-huu-khang.jpg', FALSE);
INSERT INTO public.users VALUES ('757f4e94-5628-4bec-b302-0f4af505e38b', 'Tran Bao Anh', '1983-05-09', 'FEMALE', '0912345678', 'tranbaoanh@saigonmedical.vn', '88 Nguyen Van Troi, Phu Nhuan, HCMC', 'doctor-tran-bao-anh.png', FALSE);
INSERT INTO public.users VALUES ('84abeaef-2551-4f18-9e74-ff28c6e5bb04', 'Do Quang Nam', '1975-11-14', 'MALE', '0906778899', 'doquangnam@viethealth.vn', '120 Le Loi, Hue', 'doctor-do-quang-nam.jpg', FALSE);

INSERT INTO public.users VALUES ('3805ed01-a8e1-40db-a43c-ed412ad81810', 'Ha Van Quyet', '1970-07-12', 'MALE', '0908666123', 'ha.vanquyet@vietduc.vn', '1 Phuong Mai, Dong Da, Hanoi', 'doctor-ha-van-quyet.jpg', FALSE);
INSERT INTO public.users VALUES ('a2f667d3-2ffd-456a-be1e-65b781545035', 'Duong Van Tien', '1978-11-02', 'MALE', '0908777345', 'duongvantien@cardio.vn', '16 Lang Ha, Dong Da, Hanoi', 'doctor-duong-van-tien.jpg', FALSE);
INSERT INTO public.users VALUES ('409a54af-77a6-4361-bb66-a7d6a4d62a28', 'Nguyen Dang Dung', '1968-04-18', 'MALE', '0908333123', 'nguyendangdung@chinhhinh.vn', '78 Giai Phong, Hoang Mai, Hanoi', 'doctor-nguyen-dang-dung.jpg', FALSE);
INSERT INTO public.users VALUES ('41224aec-70f8-4c28-91d3-e137f08ec131', 'Nguyen Trung Duong', '1980-01-25', 'MALE', '0908666890', 'nguyentrungduong@vietduc.vn', '40 Trang Thi, Hoan Kiem, Hanoi', 'doctor-nguyen-trung-duong.jpg', FALSE);
INSERT INTO public.users VALUES ('9b876fdb-b18e-42ce-a5ff-30f2b9073750', 'Nguyen Van Ly', '1975-05-30', 'MALE', '0908555789', 'nguyenvanly@endocrine.vn', '12 Le Duan, Ba Dinh, Hanoi', 'doctor-nguyen-van-ly.jpg', FALSE);
INSERT INTO public.users VALUES ('68ff3177-a49c-4846-b3a8-17a1777b850f', 'Dang Thi Nhu Quynh', '1984-09-14', 'FEMALE', '0912444668', 'nhuquynh.dang@obgyn.vn', '25 Thai Thinh, Dong Da, Hanoi', 'doctor-dang-thi-nhu-quynh.png', FALSE);
INSERT INTO public.users VALUES ('0b9ef7e1-8574-46bc-b497-9572b1966cf0', 'Nguyen Duy Khanh', '1986-12-07', 'MALE', '0907111444', 'nguyenduykhanh@urology.vn', '55 Tran Hung Dao, Hoan Kiem, Hanoi', 'doctor-nguyen-duy-khanh.jpg', FALSE);

-- Users with role: ADMIN
INSERT INTO public.users VALUES ('600b32f6-b78b-4405-828f-40790361792c', 'Nguyen Van A', '1990-05-15', 'MALE', '0901234567', 'nguyenvana@example.com', '123 Le Loi, District 1, HCMC', NULL, FALSE);
INSERT INTO public.users VALUES ('12f4ccec-e17b-4f86-8d54-efd0b523095e', 'Vo Thi Lan', '1990-02-17', 'FEMALE', '0908222333', 'lan.vo@bookingcare.vn', '45 Cach Mang Thang 8, District 3, HCMC', 'admin-vo-thi-lan.png', FALSE);
INSERT INTO public.users VALUES ('a482f9fb-1da9-4a44-b8af-4346e2f30a1c', 'Dang Hoang Son', '1987-07-03', 'MALE', '0909555777', 'son.dang@bookingcare.vn', '10 Tran Hung Dao, Hoan Kiem, Hanoi', 'admin-dang-hoang-son.png', FALSE);

-- Users with role: PATIENT
INSERT INTO public.users VALUES ('d600f11c-0c0c-4ce7-8994-5a5f2bc9e76b', 'Nguyá»…n QuÃ½ HÃ¹ng', '1995-06-15', 'MALE', '0908171822', 'nguyenquyhung@gmail.com', '12 Vo Van Kiet', 'https://example.com/uploads/avatar_nguyenvana.jpg', FALSE);
INSERT INTO public.users VALUES ('d5624f9b-ff8c-4329-b2b3-eb0f91e848e1', 'Hoang Gia Phuc', '1992-09-12', 'MALE', '0938001101', 'phuc.hoang@example.com', '25 Nguyen Xi, Binh Thanh, HCMC', 'patient-hoang-gia-phuc.jpg', FALSE);
INSERT INTO public.users VALUES ('a4aa0bcb-e240-421e-9692-c5ce6eff05b5', 'Nguyen Thi Kim', '1998-04-25', 'FEMALE', '0911222334', 'kim.nguyen@example.com', '67 Tran Khac Chan, Hanoi', 'patient-nguyen-thi-kim.jpg', FALSE);
INSERT INTO public.users VALUES ('79c76311-7acf-4049-adb9-8fa64c82d160', 'Le Thanh Tung', '1991-01-08', 'MALE', '0902333445', 'tung.le@example.com', '5 Phan Boi Chau, Da Nang', NULL, FALSE);
INSERT INTO public.users VALUES ('be70213b-d496-4b93-97f5-ad5d64a189e6', 'Tran My Duyen', '1996-07-19', 'FEMALE', '0933111222', 'duyen.tran@example.com', '102 Pasteur, District 3, HCMC', 'patient-tran-my-duyen.png', FALSE);
INSERT INTO public.users VALUES ('b5bfd2b7-5633-4c64-8013-dbfab2260d7b', 'Pham Dang Khoa', '1989-12-30', 'MALE', '0917888990', 'khoa.pham@example.com', '43 Nguyen Thai Hoc, Hoi An', NULL, FALSE);
INSERT INTO public.users VALUES ('dd957752-1fb1-4037-9c37-1dfc663a3c3f', 'Do Mai Anh', '1994-03-22', 'FEMALE', '0989111222', 'anh.do@example.com', '220 Kim Ma, Ba Dinh, Hanoi', 'patient-do-mai-anh.jpg', FALSE);
INSERT INTO public.users VALUES ('752eae05-a1e7-41c7-b511-2dd88a77cda7', 'Dang Quoc Viet', '1987-10-02', 'MALE', '0901444556', 'viet.dang@example.com', '9 Tran Cao Van, Da Nang', NULL, FALSE);
INSERT INTO public.users VALUES ('f9c9254a-998d-45be-a1c8-64c954b227f6', 'Bui Ngoc Chau', '1993-05-27', 'FEMALE', '0977333444', 'chau.bui@example.com', '18B Nguyen Hue, District 1, HCMC', 'patient-bui-ngoc-chau.jpg', FALSE);
INSERT INTO public.users VALUES ('c1730f7c-6fb1-4dc9-a447-66391d721418', 'Pham Huynh Long', '1990-09-05', 'MALE', '0933666777', 'long.pham@example.com', '56 Cach Mang Thang 8, District 3, HCMC', 'patient-pham-huynh-long.jpg', FALSE);
INSERT INTO public.users VALUES ('939e6fe2-1d8a-4218-8c9c-8f9e7aafa8e9', 'Vu Thanh Ha', '1999-12-18', 'FEMALE', '0968111000', 'ha.vu@example.com', '12 Nguyen Du, Hai Ba Trung, Hanoi', 'patient-vu-thanh-ha.png', FALSE);


-- Accounts with role: DOCTOR
INSERT INTO public.accounts VALUES ('721c98db-18af-4fc4-8d9f-f0e4c8a39936', 'd4183f14-9953-4401-b2d9-b08c3f72cfcd', 'doctor', '$2a$10$qIbI1lqVEw2jepEIzW3Ween2upd87EoDDWSArlqJIa0.aDN0i5G5.', 'role-doc-1', FALSE);
INSERT INTO public.accounts VALUES ('da7b1b73-7e6c-46a9-b0fb-de829017a114', '6d3d7b15-33e8-4088-9026-f5aa8ea99eba', 'dr.leminhtuan', '$2a$10$qIbI1lqVEw2jepEIzW3Ween2upd87EoDDWSArlqJIa0.aDN0i5G5.', 'role-doc-1', FALSE);
INSERT INTO public.accounts VALUES ('f2e67275-6193-48af-8c61-76e0a5b9ee6c', 'bcfcd0af-2459-4a1f-b038-247a3d48a572', 'dr.phamthihoa', '$2a$10$qIbI1lqVEw2jepEIzW3Ween2upd87EoDDWSArlqJIa0.aDN0i5G5.', 'role-doc-1', FALSE);
INSERT INTO public.accounts VALUES ('bf1839a5-2c33-4c2e-9d0f-3a0dc244c3e2', 'c79e85ce-5e2b-49e5-8bf0-5d2529706bea', 'dr.nguyenhuukhang', '$2a$10$qIbI1lqVEw2jepEIzW3Ween2upd87EoDDWSArlqJIa0.aDN0i5G5.', 'role-doc-1', FALSE);
INSERT INTO public.accounts VALUES ('b7188355-5d27-44a3-bd93-3d262c6444e4', '757f4e94-5628-4bec-b302-0f4af505e38b', 'dr.tranbaoanh', '$2a$10$qIbI1lqVEw2jepEIzW3Ween2upd87EoDDWSArlqJIa0.aDN0i5G5.', 'role-doc-1', FALSE);
INSERT INTO public.accounts VALUES ('bc842d0c-c6a8-49cb-ba2a-0695e55001cf', '84abeaef-2551-4f18-9e74-ff28c6e5bb04', 'dr.doquangnam', '$2a$10$qIbI1lqVEw2jepEIzW3Ween2upd87EoDDWSArlqJIa0.aDN0i5G5.', 'role-doc-1', FALSE);

INSERT INTO public.accounts VALUES ('120da3e0-5b8a-4ed4-85b5-0cd8e7ff2ee6', '3805ed01-a8e1-40db-a43c-ed412ad81810', 'dr.havanquyet', '$2a$10$qIbI1lqVEw2jepEIzW3Ween2upd87EoDDWSArlqJIa0.aDN0i5G5.', 'role-doc-1', FALSE);
INSERT INTO public.accounts VALUES ('be33af95-97d1-4e32-b652-649942c4efc1', 'a2f667d3-2ffd-456a-be1e-65b781545035', 'dr.duongvantien', '$2a$10$qIbI1lqVEw2jepEIzW3Ween2upd87EoDDWSArlqJIa0.aDN0i5G5.', 'role-doc-1', FALSE);
INSERT INTO public.accounts VALUES ('8046e940-f8c8-4f95-bfd4-625c8d64af34', '409a54af-77a6-4361-bb66-a7d6a4d62a28', 'dr.nguyendangdung', '$2a$10$qIbI1lqVEw2jepEIzW3Ween2upd87EoDDWSArlqJIa0.aDN0i5G5.', 'role-doc-1', FALSE);
INSERT INTO public.accounts VALUES ('bb167d7a-f132-403c-a293-6dcc6e2f9c30', '41224aec-70f8-4c28-91d3-e137f08ec131', 'dr.nguyentrungduong', '$2a$10$qIbI1lqVEw2jepEIzW3Ween2upd87EoDDWSArlqJIa0.aDN0i5G5.', 'role-doc-1', FALSE);
INSERT INTO public.accounts VALUES ('3fc44a45-d799-46ce-81e9-93802b1dc592', '9b876fdb-b18e-42ce-a5ff-30f2b9073750', 'dr.nguyenvanly', '$2a$10$qIbI1lqVEw2jepEIzW3Ween2upd87EoDDWSArlqJIa0.aDN0i5G5.', 'role-doc-1', FALSE);
INSERT INTO public.accounts VALUES ('0e5d9454-f46a-4127-8bf2-7ff1d0ee4275', '68ff3177-a49c-4846-b3a8-17a1777b850f', 'dr.nhuquynh', '$2a$10$qIbI1lqVEw2jepEIzW3Ween2upd87EoDDWSArlqJIa0.aDN0i5G5.', 'role-doc-1', FALSE);
INSERT INTO public.accounts VALUES ('c66e14c8-3178-4c68-9f1a-9e63fcf0ef30', '0b9ef7e1-8574-46bc-b497-9572b1966cf0', 'dr.nguyenduykhanh', '$2a$10$qIbI1lqVEw2jepEIzW3Ween2upd87EoDDWSArlqJIa0.aDN0i5G5.', 'role-doc-1', FALSE);

-- Accounts with role: ADMIN
INSERT INTO public.accounts VALUES ('696c46a4-35f0-4572-9198-116e3d0c17d2', '600b32f6-b78b-4405-828f-40790361792c', 'admin', '$2a$10$IkCAT7yWNMt/yPgRpS/hvevnMGQ/HN1tSSpKDkyYpQvIFGjAd.p1W', 'role-adm-1', FALSE);
INSERT INTO public.accounts VALUES ('c890fa6c-232d-4819-b957-1317699cd7a4', '12f4ccec-e17b-4f86-8d54-efd0b523095e', 'vo.lan.admin', '$2a$10$IkCAT7yWNMt/yPgRpS/hvevnMGQ/HN1tSSpKDkyYpQvIFGjAd.p1W', 'role-adm-1', FALSE);
INSERT INTO public.accounts VALUES ('6638ebd4-5c75-45fc-adcf-895cd5e88f7f', 'a482f9fb-1da9-4a44-b8af-4346e2f30a1c', 'son.dang.admin', '$2a$10$IkCAT7yWNMt/yPgRpS/hvevnMGQ/HN1tSSpKDkyYpQvIFGjAd.p1W', 'role-adm-1', FALSE);

-- Accounts with role: PATIENT
INSERT INTO public.accounts VALUES ('51883de6-fbe4-4f0d-a228-361253010e8d', 'd600f11c-0c0c-4ce7-8994-5a5f2bc9e76b', 'quyhung', '$2a$10$U.xUtCBQzAy65Xzacv78r.dj1E2105/VfWUwHCeKmDKCFflkuP38W', 'role-pat-1', FALSE);
INSERT INTO public.accounts VALUES ('e509f466-d557-456f-b159-0e7187b25116', 'd5624f9b-ff8c-4329-b2b3-eb0f91e848e1', 'phuc.hoang', '$2a$10$U.xUtCBQzAy65Xzacv78r.dj1E2105/VfWUwHCeKmDKCFflkuP38W', 'role-pat-1', FALSE);
INSERT INTO public.accounts VALUES ('bd9d4774-bd3a-4f60-b633-9960913d2df1', 'a4aa0bcb-e240-421e-9692-c5ce6eff05b5', 'kim.nguyen', '$2a$10$U.xUtCBQzAy65Xzacv78r.dj1E2105/VfWUwHCeKmDKCFflkuP38W', 'role-pat-1', FALSE);
INSERT INTO public.accounts VALUES ('e3514588-eec9-4437-b59e-7484c1d58ed5', '79c76311-7acf-4049-adb9-8fa64c82d160', 'tung.le', '$2a$10$U.xUtCBQzAy65Xzacv78r.dj1E2105/VfWUwHCeKmDKCFflkuP38W', 'role-pat-1', FALSE);
INSERT INTO public.accounts VALUES ('86f65583-4120-4a2d-bb4d-8f985871c435', 'be70213b-d496-4b93-97f5-ad5d64a189e6', 'duyen.tran', '$2a$10$U.xUtCBQzAy65Xzacv78r.dj1E2105/VfWUwHCeKmDKCFflkuP38W', 'role-pat-1', FALSE);
INSERT INTO public.accounts VALUES ('9f197154-5b13-4306-9fdc-9632211083dd', 'b5bfd2b7-5633-4c64-8013-dbfab2260d7b', 'khoa.pham', '$2a$10$U.xUtCBQzAy65Xzacv78r.dj1E2105/VfWUwHCeKmDKCFflkuP38W', 'role-pat-1', FALSE);
INSERT INTO public.accounts VALUES ('4eace631-8765-4b34-a629-62f2bc72934d', 'dd957752-1fb1-4037-9c37-1dfc663a3c3f', 'anh.do', '$2a$10$U.xUtCBQzAy65Xzacv78r.dj1E2105/VfWUwHCeKmDKCFflkuP38W', 'role-pat-1', FALSE);
INSERT INTO public.accounts VALUES ('6318f0b6-ab48-4363-9ca1-de6336be9fbd', '752eae05-a1e7-41c7-b511-2dd88a77cda7', 'viet.dang', '$2a$10$U.xUtCBQzAy65Xzacv78r.dj1E2105/VfWUwHCeKmDKCFflkuP38W', 'role-pat-1', FALSE);
INSERT INTO public.accounts VALUES ('cb14502a-22a7-46ad-836e-de0ce135012a', 'f9c9254a-998d-45be-a1c8-64c954b227f6', 'chau.bui', '$2a$10$U.xUtCBQzAy65Xzacv78r.dj1E2105/VfWUwHCeKmDKCFflkuP38W', 'role-pat-1', FALSE);
INSERT INTO public.accounts VALUES ('17820779-0ba2-4771-83b4-639a5da14641', 'c1730f7c-6fb1-4dc9-a447-66391d721418', 'long.pham', '$2a$10$U.xUtCBQzAy65Xzacv78r.dj1E2105/VfWUwHCeKmDKCFflkuP38W', 'role-pat-1', FALSE);
INSERT INTO public.accounts VALUES ('e5d3ac1b-a799-498d-8bbc-41cf9f2cdc49', '939e6fe2-1d8a-4218-8c9c-8f9e7aafa8e9', 'ha.vu', '$2a$10$U.xUtCBQzAy65Xzacv78r.dj1E2105/VfWUwHCeKmDKCFflkuP38W', 'role-pat-1', FALSE);


-- Users with role: CLINIC_ADMIN
INSERT INTO public.users VALUES ('f8c3e9d2-4b7a-4d9e-8f2c-5a9b1c3d4e5f', 'Tran Van Chien', '1988-03-15', 'MALE', '0912456789', 'tran.vanchien@clinic.vn', '55 Tran Khac Chan, Hanoi', 'clinic-admin-tran-van-chien.jpg', FALSE);
INSERT INTO public.users VALUES ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'Nguyen Thi Thuy', '1990-07-22', 'FEMALE', '0933567890', 'nguyen.thuy@clinic.vn', '123 Pham Ngu Lao, District 1, HCMC', 'clinic-admin-nguyen-thi-thuy.jpg', FALSE);
INSERT INTO public.users VALUES ('b5c6d7e8-f9a0-1b2c-3d4e-5f6a7b8c9d0e', 'Le Van Phuc', '1985-11-08', 'MALE', '0917889900', 'le.vanphuc@hospital.vn', '88 Cach Mang Thang 8, District 1, HCMC', 'clinic-admin-le-van-phuc.jpg', FALSE);
INSERT INTO public.users VALUES ('c9d0e1f2-3a4b-5c6d-7e8f-9a0b1c2d3e4f', 'Pham Thi Linh', '1992-02-14', 'FEMALE', '0909111222', 'pham.linh@clinic.vn', '45 Hai Ba Trung, Hanoi', 'clinic-admin-pham-thi-linh.jpg', FALSE);
INSERT INTO public.users VALUES ('d3e4f5a6-b7c8-9d0e-1f2a-3b4c5d6e7f8a', 'Hoang Duc Manh', '1987-09-30', 'MALE', '0905223344', 'hoang.ducmanh@clinic.vn', '12 Nguyen Thai Hoc, Hoi An', 'clinic-admin-hoang-duc-manh.jpg', FALSE);

-- Accounts with role: CLINIC_ADMIN
INSERT INTO public.accounts VALUES ('e4f5a6b7-c8d9-0e1f-2a3b-4c5d6e7f8a9b', 'f8c3e9d2-4b7a-4d9e-8f2c-5a9b1c3d4e5f', 'tran.chien.admin', '$2a$10$qIbI1lqVEw2jepEIzW3Ween2upd87EoDDWSArlqJIa0.aDN0i5G5.', 'role-clinic-adm-1', FALSE);
INSERT INTO public.accounts VALUES ('f5a6b7c8-d9e0-1f2a-3b4c-5d6e7f8a9b0c', 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'nguyen.thuy.admin', '$2a$10$qIbI1lqVEw2jepEIzW3Ween2upd87EoDDWSArlqJIa0.aDN0i5G5.', 'role-clinic-adm-1', FALSE);
INSERT INTO public.accounts VALUES ('a6b7c8d9-e0f1-2a3b-4c5d-6e7f8a9b0c1d', 'b5c6d7e8-f9a0-1b2c-3d4e-5f6a7b8c9d0e', 'le.phuc.admin', '$2a$10$qIbI1lqVEw2jepEIzW3Ween2upd87EoDDWSArlqJIa0.aDN0i5G5.', 'role-clinic-adm-1', FALSE);
INSERT INTO public.accounts VALUES ('b7c8d9e0-f1a2-3b4c-5d6e-7f8a9b0c1d2e', 'c9d0e1f2-3a4b-5c6d-7e8f-9a0b1c2d3e4f', 'pham.linh.admin', '$2a$10$qIbI1lqVEw2jepEIzW3Ween2upd87EoDDWSArlqJIa0.aDN0i5G5.', 'role-clinic-adm-1', FALSE);
INSERT INTO public.accounts VALUES ('c8d9e0f1-a2b3-4c5d-6e7f-8a9b0c1d2e3f', 'd3e4f5a6-b7c8-9d0e-1f2a-3b4c5d6e7f8a', 'hoang.manh.admin', '$2a$10$qIbI1lqVEw2jepEIzW3Ween2upd87EoDDWSArlqJIa0.aDN0i5G5.', 'role-clinic-adm-1', FALSE);

INSERT INTO public.refresh_tokens VALUES ('rft-301', 'f8c3e9d2-4b7a-4d9e-8f2c-5a9b1c3d4e5f', 'secure_refresh_token_for_user_101_xyz123', false);
INSERT INTO public.refresh_tokens VALUES ('rft-302', 'd3e4f5a6-b7c8-9d0e-1f2a-3b4c5d6e7f8a', 'secure_refresh_token_for_user_102_abc456', false);




