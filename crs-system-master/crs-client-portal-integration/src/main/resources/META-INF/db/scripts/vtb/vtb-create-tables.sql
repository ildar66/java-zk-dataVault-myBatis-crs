CREATE TABLE CL_INDUSTRY(
  CL_INDUSTRYID  INTEGER                        NOT NULL,
  RMV            NUMBER(1)                      DEFAULT 0                     NOT NULL,
  MODIFYSTAMP    TIMESTAMP(6)                   DEFAULT SYSTIMESTAMP          NOT NULL,
  MODIFYUSER     INTEGER                        DEFAULT 1,
  NAME           VARCHAR2(512 CHAR),
  NAME_EN        VARCHAR2(512 CHAR),
  CODE           VARCHAR2(30 CHAR),
  IMG_CODE       VARCHAR2(7 CHAR)               DEFAULT '0'                   NOT NULL
)
/
COMMENT ON TABLE CL_INDUSTRY IS 'Справочник отраслей'
/
COMMENT ON COLUMN CL_INDUSTRY.CL_INDUSTRYID IS 'Первичный ключ'
/
COMMENT ON COLUMN CL_INDUSTRY.RMV IS 'Является ли запись устаревшей (0 - не является, 1-удалена)'
/
COMMENT ON COLUMN CL_INDUSTRY.NAME IS 'Наименование'
/
COMMENT ON COLUMN CL_INDUSTRY.NAME_EN IS 'Наименование литиницей'
/

CREATE TABLE CL_TYPE(
  CL_TYPEID    INTEGER                          NOT NULL,
  RMV          NUMBER(1)                        DEFAULT 0                     NOT NULL,
  MODIFYUSER   INTEGER                          DEFAULT 1,
  MODIFYSTAMP  TIMESTAMP(6)                     DEFAULT SYSTIMESTAMP          NOT NULL,
  NAME         VARCHAR2(128 CHAR),
  NAME_EN      VARCHAR2(128 CHAR),
  PRIORITY     INTEGER
)
/
COMMENT ON TABLE CL_TYPE IS 'Типы клиента (Проспект, Клиент, Бывший клент, Технический клиент)'
/
COMMENT ON COLUMN CL_TYPE.CL_TYPEID IS 'Первичный ключ'
/
COMMENT ON COLUMN CL_TYPE.RMV IS 'Является ли запись устаревшей (0 - не является, 1-удалена)'
/
COMMENT ON COLUMN CL_TYPE.NAME IS 'Наименование типа'
/
COMMENT ON COLUMN CL_TYPE.NAME_EN IS 'Наименование типа латиницей'
/

CREATE TABLE COUNTRYISO(
  COUNTRYISOID       INTEGER                    NOT NULL,
  RMV                NUMBER(1)                  DEFAULT 0                     NOT NULL,
  MODIFYSTAMP        TIMESTAMP(6)               DEFAULT SYSTIMESTAMP          NOT NULL,
  MODIFYUSER         INTEGER                    DEFAULT 1,
  CODE_A2            VARCHAR2(10 CHAR),
  CODE_A3            VARCHAR2(10 CHAR),
  NUM_CODE           NUMBER,
  COUNTRYISO_RISKID  INTEGER,
  FLAG               BLOB,
  NAME               VARCHAR2(128 CHAR),
  NAME_EN            VARCHAR2(128 CHAR)
)
/
COMMENT ON TABLE COUNTRYISO IS 'Справочник стран'
/
COMMENT ON COLUMN COUNTRYISO.COUNTRYISOID IS 'Первичный ключ'
/
COMMENT ON COLUMN COUNTRYISO.RMV IS 'Является ли запись устаревшей (0 - не является, 1-удалена)'
/
COMMENT ON COLUMN COUNTRYISO.CODE_A2 IS 'Буквенный код A2 страны'
/
COMMENT ON COLUMN COUNTRYISO.CODE_A3 IS 'Буквенный код A3 страны'
/
COMMENT ON COLUMN COUNTRYISO.NUM_CODE IS 'Цифровой код страны'
/
COMMENT ON COLUMN COUNTRYISO.COUNTRYISO_RISKID IS 'ID Группы риска'
/
COMMENT ON COLUMN COUNTRYISO.FLAG IS 'Изображение флага страны'
/
COMMENT ON COLUMN COUNTRYISO.NAME IS 'Наименование'
/
COMMENT ON COLUMN COUNTRYISO.NAME_EN IS 'Наименование латиницей'
/

CREATE TABLE CURRENCYISO(
  CURRENCYISOID  INTEGER                        NOT NULL,
  RMV            NUMBER(1)                      DEFAULT 0                     NOT NULL,
  MODIFYUSER     INTEGER                        DEFAULT 1,
  MODIFYSTAMP    TIMESTAMP(6)                   DEFAULT SYSTIMESTAMP          NOT NULL,
  CODE_NUM       NUMBER,
  CODE           VARCHAR2(6 CHAR),
  NAME           VARCHAR2(128 CHAR),
  NAME_SHORT     VARCHAR2(16 CHAR),
  NAME_EN        VARCHAR2(128 CHAR),
  NAME_SHORT_EN  VARCHAR2(16 CHAR)
)
/
COMMENT ON TABLE CURRENCYISO IS 'Справочник валют'
/
COMMENT ON COLUMN CURRENCYISO.CURRENCYISOID IS 'Код валюты'
/
COMMENT ON COLUMN CURRENCYISO.RMV IS 'Является ли запись устаревшей (0 - не является, 1-удалена)'
/
COMMENT ON COLUMN CURRENCYISO.CODE_NUM IS 'Цифровой код валюты'
/
COMMENT ON COLUMN CURRENCYISO.CODE IS 'Код валюты'
/
COMMENT ON COLUMN CURRENCYISO.NAME IS 'Наименование валюты'
/
COMMENT ON COLUMN CURRENCYISO.NAME_SHORT IS 'Короткое наименование валюты'
/
COMMENT ON COLUMN CURRENCYISO.NAME_EN IS 'Наименование латиницей'
/
COMMENT ON COLUMN CURRENCYISO.NAME_SHORT_EN IS 'Короткое наименование латиницей'
/

CREATE TABLE DEPARTMENT(
  DEPARTMENTID  INTEGER                         NOT NULL,
  RMV           NUMBER(1)                       DEFAULT 0                     NOT NULL,
  CREATEUSER    INTEGER                         DEFAULT 1,
  CREATESTAMP   TIMESTAMP(6)                    DEFAULT SYSTIMESTAMP          NOT NULL,
  MODIFYUSER    INTEGER                         DEFAULT 1,
  MODIFYSTAMP   TIMESTAMP(6)                    DEFAULT SYSTIMESTAMP          NOT NULL,
  NAME          VARCHAR2(255 CHAR),
  NAME_EN       VARCHAR2(255 CHAR),
  DEP_TYPEID    INTEGER                         NOT NULL,
  VTB_ORGID     INTEGER,
  CODE          VARCHAR2(30 CHAR),
  FULL_NAME     VARCHAR2(255 CHAR),
  FULL_NAME_EN  VARCHAR2(255 CHAR),
  SLXID         VARCHAR2(12 CHAR)
)
/
COMMENT ON TABLE DEPARTMENT IS 'Реальная структура ВТБ'
/
COMMENT ON COLUMN DEPARTMENT.DEPARTMENTID IS 'Первичный ключ'
/
COMMENT ON COLUMN DEPARTMENT.RMV IS 'Является ли запись устаревшей (0 - не является, 1-удалена)'
/
COMMENT ON COLUMN DEPARTMENT.NAME IS 'Наименование подразделения'
/
COMMENT ON COLUMN DEPARTMENT.NAME_EN IS 'Наименование подразделения латиницей'
/
COMMENT ON COLUMN DEPARTMENT.DEP_TYPEID IS 'Код типа подразделения'
/
COMMENT ON COLUMN DEPARTMENT.VTB_ORGID IS 'Код цчастника группы ВТБ к которой относится департамент'
/
COMMENT ON COLUMN DEPARTMENT.CODE IS 'Код подразделения(для служебных нужд (константа))'
/

CREATE TABLE DEP_CATEGORY(
  DEP_CATEGORYID  INTEGER                       NOT NULL,
  RMV             NUMBER(1)                     DEFAULT 0                     NOT NULL,
  MODIFYSTAMP     TIMESTAMP(6)                  DEFAULT SYSTIMESTAMP          NOT NULL,
  MODIFYUSER      INTEGER                       DEFAULT 1,
  NAME            VARCHAR2(128 CHAR),
  NAME_EN         VARCHAR2(128 CHAR)
)
/
COMMENT ON TABLE DEP_CATEGORY IS 'Справочник категорий'
/
COMMENT ON COLUMN DEP_CATEGORY.DEP_CATEGORYID IS 'Первичный ключ'
/
COMMENT ON COLUMN DEP_CATEGORY.RMV IS 'Является ли запись устаревшей (0 - не является, 1-удалена)'
/
COMMENT ON COLUMN DEP_CATEGORY.NAME IS 'Наименование'
/
COMMENT ON COLUMN DEP_CATEGORY.NAME_EN IS 'Наименование латиницей'
/


CREATE TABLE DEP_LINK(
  DEP_LINKID       INTEGER                      NOT NULL,
  RMV              NUMBER(1)                    DEFAULT 0                     NOT NULL,
  MODIFYUSER       INTEGER                      DEFAULT 1,
  MODIFYSTAMP      TIMESTAMP(6)                 DEFAULT SYSTIMESTAMP          NOT NULL,
  PARENTID         INTEGER,
  DEPARTMENTID     INTEGER,
  DEP_LINK_TYPEID  INTEGER,
  FOR_CHILD        VARCHAR2(1 CHAR)             DEFAULT 'F'                   NOT NULL,
  PARENT_LINKID    INTEGER,
  PRIORITY         INTEGER,
  HIDDEN           NUMBER(1)                    DEFAULT 0                     NOT NULL,
  GLOBAL_PRIORITY  INTEGER                      DEFAULT 0
)
/
COMMENT ON TABLE DEP_LINK IS 'Связи между подразделениями'
/
COMMENT ON COLUMN DEP_LINK.RMV IS 'Является ли запись устаревшей (0 - не является, 1-удалена)'
/
COMMENT ON COLUMN DEP_LINK.GLOBAL_PRIORITY IS 'Автоматически рассчитанный сквозной приоритет'
/

CREATE TABLE CL_EARN_SEG(
  CL_EARN_SEGID  INTEGER                        NOT NULL,
  RMV            NUMBER(1)                      DEFAULT 0                     NOT NULL,
  MODIFYUSER     INTEGER                        DEFAULT 1,
  MODIFYSTAMP    TIMESTAMP(6)                   DEFAULT SYSTIMESTAMP          NOT NULL,
  NAME           VARCHAR2(128 CHAR),
  NAME_EN        VARCHAR2(128 CHAR),
  DIAP_MIN       NUMBER,
  DIAP_MAX       NUMBER,
  CURRENCYID     INTEGER                        NOT NULL
)
/
COMMENT ON TABLE CL_EARN_SEG IS 'Справочник сегментов выручки'
/
COMMENT ON COLUMN CL_EARN_SEG.CL_EARN_SEGID IS 'Первичный ключ'
/
COMMENT ON COLUMN CL_EARN_SEG.RMV IS 'Является ли запись устаревшей (0 - не является, 1-удалена)'
/
COMMENT ON COLUMN CL_EARN_SEG.NAME IS 'Наименование'
/
COMMENT ON COLUMN CL_EARN_SEG.NAME_EN IS 'Наименование латиницей'
/
COMMENT ON COLUMN CL_EARN_SEG.DIAP_MIN IS 'Нижняя граница диапазона'
/
COMMENT ON COLUMN CL_EARN_SEG.DIAP_MAX IS 'Верхняя граница диапазона'
/
COMMENT ON COLUMN CL_EARN_SEG.CURRENCYID IS 'Валюта'
/

CREATE TABLE CL_OPF(
  CL_OPFID      INTEGER                         NOT NULL,
  RMV           NUMBER(1)                       DEFAULT 0                     NOT NULL,
  MODIFYSTAMP   TIMESTAMP(6)                    DEFAULT SYSTIMESTAMP          NOT NULL,
  MODIFYUSER    INTEGER                         DEFAULT 1,
  COUNTRYISOID  INTEGER,
  OKFS_CODE     NUMBER,
  NAME          VARCHAR2(128 CHAR),
  NAME_EN       VARCHAR2(128 CHAR)
)
/
COMMENT ON TABLE CL_OPF IS 'Справочник организационно-правовых форм'
/
COMMENT ON COLUMN CL_OPF.CL_OPFID IS 'Первичный ключ'
/
COMMENT ON COLUMN CL_OPF.RMV IS 'Является ли запись устаревшей (0 - не является, 1-удалена)'
/
COMMENT ON COLUMN CL_OPF.COUNTRYISOID IS 'ID Страны'
/
COMMENT ON COLUMN CL_OPF.OKFS_CODE IS 'Код ОКФС'
/
COMMENT ON COLUMN CL_OPF.NAME IS 'Наименование'
/
COMMENT ON COLUMN CL_OPF.NAME_EN IS 'Наименование латиницей'
/

CREATE TABLE CL_GLOBAL(
  CL_GLOBALID      INTEGER                      NOT NULL,
  PARENTID         INTEGER,
  CL_GLOBAL_CUTID  NUMBER(2)                    DEFAULT 1                     NOT NULL,
  SLXID            VARCHAR2(12 CHAR),
  RMV              NUMBER(1)                    DEFAULT 0                     NOT NULL,
  CREATEUSER       INTEGER                      DEFAULT 1,
  CREATESTAMP      TIMESTAMP(6)                 DEFAULT SYSTIMESTAMP          NOT NULL,
  MODIFYUSER       INTEGER                      DEFAULT 1,
  MODIFYSTAMP      TIMESTAMP(6)                 DEFAULT SYSTIMESTAMP          NOT NULL,
  NAME             VARCHAR2(255 CHAR),
  NAME_EN          VARCHAR2(255 CHAR),
  NAME_FULL        VARCHAR2(512 CHAR),
  NAME_FULL_EN     VARCHAR2(512 CHAR),
  WEB_ADDRESS      VARCHAR2(255 CHAR),
  DEP_CATEGORYID   INTEGER,
  CL_INDUSTRYID    INTEGER,
  BASE_INDUSTRYID  INTEGER,
  REG_COUNTRYID    INTEGER,
  REG_NUM          VARCHAR2(32 CHAR),
  REG_NUM_EXT      VARCHAR2(32 CHAR),
  TAX_NUM          VARCHAR2(32 CHAR),
  CL_STATUSID      INTEGER,
  FOR_ADMIN        VARCHAR2(1 CHAR)             DEFAULT 'F'                   NOT NULL,
  CL_OWNERSHIPID   INTEGER,
  CL_OPFID         INTEGER,
  CL_LAWYERID      INTEGER,
  IMAGEID          INTEGER,
  EMPLOYEES        VARCHAR2(32 CHAR),
  EMPLOYEES_YEAR   INTEGER,
  EMPLOYEES_SRCID  INTEGER
)
/

COMMENT ON TABLE CL_GLOBAL IS 'Таблица для хранения глобальной карточки клиента'
/
COMMENT ON COLUMN CL_GLOBAL.CL_GLOBALID IS 'Первичный ключ'
/
COMMENT ON COLUMN CL_GLOBAL.PARENTID IS 'ID Группы компаний'
/
COMMENT ON COLUMN CL_GLOBAL.CL_GLOBAL_CUTID IS 'Тип порезки таблицы (Клиент, Группа, Филиал и т.п.)'
/
COMMENT ON COLUMN CL_GLOBAL.RMV IS 'Является ли запись устаревшей (0 - не является, 1-удалена)'
/
COMMENT ON COLUMN CL_GLOBAL.CREATEUSER IS 'Дата создания'
/
COMMENT ON COLUMN CL_GLOBAL.CREATESTAMP IS 'Создатель'
/
COMMENT ON COLUMN CL_GLOBAL.MODIFYUSER IS 'Пользователь редактировавший запись'
/
COMMENT ON COLUMN CL_GLOBAL.MODIFYSTAMP IS 'Реальная дата редактирования (системная)'
/
COMMENT ON COLUMN CL_GLOBAL.NAME IS 'Наименование'
/
COMMENT ON COLUMN CL_GLOBAL.NAME_EN IS 'Наименование латиницей'
/
COMMENT ON COLUMN CL_GLOBAL.NAME_FULL IS 'Полное юридическое наименование'
/
COMMENT ON COLUMN CL_GLOBAL.NAME_FULL_EN IS 'Полное юридическое наименование латиницей'
/
COMMENT ON COLUMN CL_GLOBAL.WEB_ADDRESS IS 'Web адрес'
/
COMMENT ON COLUMN CL_GLOBAL.DEP_CATEGORYID IS 'ID Категории бизнеса'
/
COMMENT ON COLUMN CL_GLOBAL.CL_INDUSTRYID IS 'ID Отрасли'
/
COMMENT ON COLUMN CL_GLOBAL.BASE_INDUSTRYID IS 'ID Базовой отрасли'
/
COMMENT ON COLUMN CL_GLOBAL.REG_COUNTRYID IS 'ID Страны регистрации'
/
COMMENT ON COLUMN CL_GLOBAL.REG_NUM IS 'Основной регистрационный номер характерный для страны регистрации'
/
COMMENT ON COLUMN CL_GLOBAL.REG_NUM_EXT IS 'Внешний аналог ОГРН'
/
COMMENT ON COLUMN CL_GLOBAL.TAX_NUM IS 'Налоговый номер характерный для страны регистрации'
/
COMMENT ON COLUMN CL_GLOBAL.CL_STATUSID IS 'ID Статуса клиента (Активный, Ликвидированный и т.п.)'
/
COMMENT ON COLUMN CL_GLOBAL.FOR_ADMIN IS 'Признак ограничения полномочий'
/
COMMENT ON COLUMN CL_GLOBAL.CL_OWNERSHIPID IS 'ID Формы собствености'
/
COMMENT ON COLUMN CL_GLOBAL.CL_OPFID IS 'ID Организационно правовой формы'
/
COMMENT ON COLUMN CL_GLOBAL.CL_LAWYERID IS 'Адвокаты нотариусы'
/
COMMENT ON COLUMN CL_GLOBAL.IMAGEID IS 'Ссылка на Логотип'
/

CREATE TABLE CL_GLOBAL_DESC(
  CL_GLOBALID     INTEGER                       NOT NULL,
  RMV             NUMBER(1)                     DEFAULT 0                     NOT NULL,
  MODIFYUSER      INTEGER                       DEFAULT 1,
  MODIFYSTAMP     TIMESTAMP(6)                  DEFAULT SYSTIMESTAMP          NOT NULL,
  DESCRIPTION     CLOB,
  DESCRIPTION_EN  CLOB
)
/

CREATE TABLE CL_GLOBAL_PARAM(
  CL_GLOBAL_PARAMID  INTEGER                    NOT NULL,
  CL_GLOBALID        INTEGER                    NOT NULL,
  RMV                NUMBER(1)                  DEFAULT 0                     NOT NULL,
  MODIFYUSER         INTEGER                    DEFAULT 1,
  MODIFYSTAMP        TIMESTAMP(6)               DEFAULT SYSTIMESTAMP          NOT NULL,
  CL_PARAM_TYPEID    INTEGER                    NOT NULL,
  VALUE              VARCHAR2(512 CHAR),
  VALUE_DT           DATE,
  VALUE_NUM          NUMBER
)
/
COMMENT ON TABLE CL_GLOBAL_PARAM IS 'Параметры глобальной карточки'
/
COMMENT ON COLUMN CL_GLOBAL_PARAM.RMV IS 'Является ли запись устаревшей (0 - не является, 1-удалена)'
/

CREATE TABLE CL_GLOBAL_PR(
  CL_GLOBAL_PRID  INTEGER                       NOT NULL,
  CL_GLOBALID     INTEGER                       NOT NULL,
  MODIFYUSER      INTEGER                       DEFAULT 1,
  MODIFYSTAMP     TIMESTAMP(6)                  DEFAULT systimestamp,
  RMV             NUMBER(1)                     DEFAULT 0                     NOT NULL,
  PR_VALUE        VARCHAR2(64 CHAR),
  CL_PR_TYPEID    INTEGER                       NOT NULL,
  COUNTRYISOID    INTEGER,
  PRIORITY        INTEGER
)
/

CREATE TABLE CL_VTBPART(
  CL_VTBPARTID      INTEGER                     NOT NULL,
  CL_GLOBALID       INTEGER                     NOT NULL,
  SLXID             VARCHAR2(12 CHAR),
  RMV               NUMBER(1)                   DEFAULT 0                     NOT NULL,
  CREATEUSER        INTEGER                     DEFAULT 1,
  CREATESTAMP       TIMESTAMP(6)                DEFAULT SYSTIMESTAMP          NOT NULL,
  MODIFYUSER        INTEGER                     DEFAULT 1,
  MODIFYSTAMP       TIMESTAMP(6)                DEFAULT SYSTIMESTAMP          NOT NULL,
  VTB_ORGID         INTEGER                     NOT NULL,
  TAX_NUM           VARCHAR2(32 CHAR),
  KIO               VARCHAR2(16 CHAR),
  NAME              VARCHAR2(255 CHAR),
  NAME_EN           VARCHAR2(255 CHAR),
  SECUREID          INTEGER,
  RESTRICT_CLASSID  INTEGER                     DEFAULT 0,
  VIRTUAL           CHAR(1 CHAR)                DEFAULT 'F'
)
/
COMMENT ON TABLE CL_VTBPART IS 'Карточка участника группы ВТБ'
/
COMMENT ON COLUMN CL_VTBPART.CL_VTBPARTID IS 'Первичный ключ'
/
COMMENT ON COLUMN CL_VTBPART.CL_GLOBALID IS 'ID Глобальной карточки участника'
/
COMMENT ON COLUMN CL_VTBPART.RMV IS 'Является ли запись устаревшей (0 - не является, 1-удалена)'
/
COMMENT ON COLUMN CL_VTBPART.VTB_ORGID IS 'ID Участника группы ВТБ'
/
COMMENT ON COLUMN CL_VTBPART.TAX_NUM IS 'Налоговый идентификатор'
/
COMMENT ON COLUMN CL_VTBPART.KIO IS 'КИО'
/
COMMENT ON COLUMN CL_VTBPART.NAME IS 'Наименование'
/
COMMENT ON COLUMN CL_VTBPART.NAME_EN IS 'Наименование латиницей'
/
COMMENT ON COLUMN CL_VTBPART.VIRTUAL IS 'Расчетные данные'
/

CREATE TABLE CL_VTBPART_PRECOUNT(
  CL_VTBPART_PRECOUNTID  INTEGER                NOT NULL,
  CL_VTBPARTID           INTEGER                NOT NULL,
  RMV                    NUMBER(1)              DEFAULT 0                     NOT NULL,
  MODIFYUSER             INTEGER                DEFAULT 1,
  MODIFYSTAMP            TIMESTAMP(6)           DEFAULT SYSTIMESTAMP          NOT NULL,
  CREATEDATE_ACC         DATE,
  CREATEUSER_ACC         INTEGER,
  MODIFYDATE_ACC         DATE,
  MODIFYUSER_ACC         INTEGER,
  MODIFYDATE_LNKD_ACC    DATE,
  MODIFYUSER_LNKD_ACC    INTEGER,
  CL_TYPEID              INTEGER                DEFAULT 1                     NOT NULL,
  BILL_DT_OPEN           DATE,
  BILL_DT_CLOSE          DATE,
  PL_QUANTITY            NUMBER                 DEFAULT 0                     NOT NULL,
  IS_PL_FORMER           VARCHAR2(1 CHAR)       DEFAULT 'F'                   NOT NULL,
  IS_PL_PLUS             VARCHAR2(1 CHAR)       DEFAULT 'F'                   NOT NULL,
  IS_PL_MINUS            VARCHAR2(1 CHAR)       DEFAULT 'F'                   NOT NULL,
  IS_BORROWER            VARCHAR2(1 CHAR)       DEFAULT 'F'                   NOT NULL,
  IS_ACTIVE              VARCHAR2(1 CHAR)       DEFAULT 'F'                   NOT NULL,
  IS_NOT_ACTIVE          VARCHAR2(1 CHAR)       DEFAULT 'F'                   NOT NULL,
  IS_PROBLEM             VARCHAR2(1 CHAR)       DEFAULT 'F'                   NOT NULL
)
/
COMMENT ON TABLE CL_VTBPART_PRECOUNT IS 'Таблица с расчетными параметрами'
/
COMMENT ON COLUMN CL_VTBPART_PRECOUNT.CL_VTBPARTID IS 'Ссылка на карточку участника'
/
COMMENT ON COLUMN CL_VTBPART_PRECOUNT.RMV IS 'Является ли запись устаревшей (0 - не является, 1-удалена)'
/
COMMENT ON COLUMN CL_VTBPART_PRECOUNT.CL_TYPEID IS 'Тип клиента/группы'
/
COMMENT ON COLUMN CL_VTBPART_PRECOUNT.BILL_DT_OPEN IS 'Дата открытия первого счета'
/
COMMENT ON COLUMN CL_VTBPART_PRECOUNT.BILL_DT_CLOSE IS 'Дата закрытия последнего счета'
/
COMMENT ON COLUMN CL_VTBPART_PRECOUNT.PL_QUANTITY IS 'Сумма PL'
/
COMMENT ON COLUMN CL_VTBPART_PRECOUNT.IS_BORROWER IS 'Признак заемщик'
/
COMMENT ON COLUMN CL_VTBPART_PRECOUNT.IS_ACTIVE IS 'Признак активный'
/
COMMENT ON COLUMN CL_VTBPART_PRECOUNT.IS_NOT_ACTIVE IS 'Признак недействующий'
/
COMMENT ON COLUMN CL_VTBPART_PRECOUNT.IS_PROBLEM IS 'Признак проблемный'
/

CREATE TABLE CL_EARN_PREF(
  CL_GLOBALID        INTEGER                    NOT NULL,
  RMV                NUMBER(1)                  DEFAULT 0                     NOT NULL,
  MODIFYUSER         INTEGER                    DEFAULT 1,
  MODIFYSTAMP        TIMESTAMP(6)               DEFAULT SYSTIMESTAMP          NOT NULL,
  MAIN_EARN_TYPEID   INTEGER,
  CL_EARN_SEGID      INTEGER,
  CL_EARN_SEG_SRCID  INTEGER,
  MAIN_EARNID        INTEGER
)
/
COMMENT ON TABLE CL_EARN_PREF IS 'Параметры выручки'
/
COMMENT ON COLUMN CL_EARN_PREF.RMV IS 'Является ли запись устаревшей (0 - не является, 1-удалена)'
/
COMMENT ON COLUMN CL_EARN_PREF.MAIN_EARN_TYPEID IS 'Основной тип выручки выбранный пользователем'
/
COMMENT ON COLUMN CL_EARN_PREF.CL_EARN_SEGID IS 'Сегмент по выручке'
/
COMMENT ON COLUMN CL_EARN_PREF.CL_EARN_SEG_SRCID IS 'Источник сегмента по выручке'
/
COMMENT ON COLUMN CL_EARN_PREF.MAIN_EARNID IS 'ID Выручки считающейся основной'
/

CREATE TABLE CL_LOCK(
  CL_LOCKID     INTEGER                         NOT NULL,
  RMV           NUMBER(1)                       DEFAULT 0                     NOT NULL,
  MODIFYSTAMP   TIMESTAMP(6)                    DEFAULT SYSTIMESTAMP          NOT NULL,
  MODIFYUSER    INTEGER                         DEFAULT 1,
  DEPARTMENTID  INTEGER                         NOT NULL,
  LOCK_TYPEID   INTEGER                         NOT NULL,
  CL_VTBPARTID  INTEGER                         NOT NULL
)
/
COMMENT ON TABLE CL_LOCK IS 'Таблица хранящая закрепление клента за подразделениями'
/
COMMENT ON COLUMN CL_LOCK.CL_LOCKID IS 'Первичный ключ'
/
COMMENT ON COLUMN CL_LOCK.RMV IS 'Является ли запись устаревшей (0 - не является, 1-удалена)'
/
COMMENT ON COLUMN CL_LOCK.MODIFYSTAMP IS 'Системная дата обновления'
/
COMMENT ON COLUMN CL_LOCK.MODIFYUSER IS 'Редактировал последним'
/
COMMENT ON COLUMN CL_LOCK.DEPARTMENTID IS 'ID Узла дерева подразделений'
/
COMMENT ON COLUMN CL_LOCK.LOCK_TYPEID IS 'Код типа закрепления'
/
COMMENT ON COLUMN CL_LOCK.CL_VTBPARTID IS 'ID Карточки участника группы ВТБ'
/
