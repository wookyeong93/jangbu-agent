create table tb_user (
user_no 		SERIAL 		not null 	primary key,
user_id 		VARCHAR(50) 	not null 	unique,
user_pwd 		VARCHAR(255) 	not null 	,
user_nm			VARCHAR(100)	null		,
refresh_token 	VARCHAR(255) 	null 		unique,
created_at 		TIMESTAMP(0) 	not null 	default CURRENT_TIMESTAMP ,
created_by 		VARCHAR(50) 	not null 	,
updated_at 		TIMESTAMP(0)	null		,
updated_by 		VARCHAR(50)		null
);

comment on table tb_user is '사용자정보';
comment on column tb_user.user_no is '사용자 번호';
comment on column tb_user.user_id is '사용자 아이디';
comment on column tb_user.user_pwd is '사용자 비밀번호';
comment on column tb_user.user_nm is '사용자 이름';
comment on column tb_user.refresh_token is '사용자 리프래쉬 토큰';
comment on column tb_user.created_at is '생성일시';
comment on column tb_user.created_by is '생성사용자아이디';
comment on column tb_user.updated_at is '수정일시';
comment on column tb_user.updated_by is '수정사용자아이디';

create table tb_group_code (
group_code		VARCHAR(50)		not null 	primary key,
group_name		VARCHAR(255)	not null	,
sort_order		smallint				not null	default 999,
use_yn			char(1)			not null	default 'Y'
);

comment on table tb_group_code is '그룹 코드 테이블';
comment on column tb_group_code.group_code is '그룹 코드';
comment on column tb_group_code.group_name is '그룹 코드 명';
comment on column tb_group_code.sort_order is '그룹 코드 순번';
comment on column tb_group_code.use_yn is '사용 여부';

create table tb_code (
code			VARCHAR(50)		not null 	,
group_code		VARCHAR(50)		not null 	REFERENCES tb_group_code ON DELETE CASCADE,
code_name		VARCHAR(255)	not null	,
sort_order		smallint				not null	default 999,
use_yn			char(1)			not null	default 'Y',
primary key (group_code , code )
);

comment on table tb_code is '그룹 코드 테이블';
comment on column tb_code.code is '코드';
comment on column tb_code.group_code is '그룹 코드';
comment on column tb_code.code_name is '코드 명';
comment on column tb_code.sort_order is '코드 순번';
comment on column tb_code.use_yn is '사용 여부';


create table tb_ledger (
ledger_no	BIGSERIAL		not null	primary key,
user_no		INT			not null 	REFERENCES tb_user ON DELETE cascade,
trx_type	VARCHAR(50)		not null	,
trx_date	DATE			not null 	default CURRENT_DATE,
trx_name	TEXT			null 		,
amount		BIGINT			not null 	default 0,
created_at	TIMESTAMP(0)	not null	default CURRENT_TIMESTAMP,
created_by	VARCHAR(50)		not null	,
updated_at 	TIMESTAMP(0)	null		,
updated_by	VARCHAR(50)		null
);


CREATE INDEX idx_ledger_01 ON tb_ledger (user_no, trx_date);
CREATE INDEX idx_ledger_02 ON tb_ledger (user_no, trx_type, trx_date);

comment on table tb_ledger is '원장 테이블';
comment on column tb_ledger.ledger_no is '원장 번호';
comment on column tb_ledger.user_no is '사용자 번호';
comment on column tb_ledger.trx_type is '구분';
comment on column tb_ledger.trx_date is '거래 일자';
comment on column tb_ledger.trx_name is '거래 명';
comment on column tb_ledger.amount is '거래 금액';
comment on column tb_ledger.created_at is '생성 일시';
comment on column tb_ledger.created_by is '생성자 아이디';
comment on column tb_ledger.updated_at is '수정 일시';
comment on column tb_ledger.updated_by is '수정자 아이디';


create table tb_daily_guide (
daily_no		BIGSERIAL		not null 	primary key,
user_no			INT				not null	REFERENCES tb_user ON DELETE cascade,
guide_dt		DATE			not null	,
context			TEXT			null		,
based_purchase	BIGINT			null	default 0,
based_sale		BIGINT			null	default 0,
based_expense	BIGINT			null	default 0,
based_profit	BIGINT			null	default 0,
model_name		VARCHAR(100)	null	,
created_at		TIMESTAMP(0)	not null	default CURRENT_TIMESTAMP,
UNIQUE(user_no, guide_dt)
);

comment on table tb_daily_guide is '데일리 가이드 테이블';
comment on column tb_daily_guide.daily_no is '가이드 번호';
comment on column tb_daily_guide.user_no is '사용자 번호';
comment on column tb_daily_guide.guide_dt is '가이드 일자';
comment on column tb_daily_guide.context is '가이드 내용';
comment on column tb_daily_guide.based_purchase is '기준 매입';
comment on column tb_daily_guide.based_sale is '기준 매출';
comment on column tb_daily_guide.based_expense is '기준 지출';
comment on column tb_daily_guide.based_profit is '기준 순수익';
comment on column tb_daily_guide.model_name is 'openAI 모델 명';
comment on column tb_daily_guide.created_at is '생성 일시';


