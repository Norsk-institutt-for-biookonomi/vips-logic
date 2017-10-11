/* 
 * Copyright (c) 2017 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the NIBIO Open Source License as published by 
 * NIBIO, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * NIBIO Open Source License for more details.
 * 
 * You should have received a copy of the NIBIO Open Source License
 * along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
 * 
 */
/**
 * Author:  treinar
 * Created: Oct 10, 2017
 */

--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.9
-- Dumped by pg_dump version 9.5.8

-- Started on 2017-10-09 16:31:19 CEST


CREATE EXTENSION postgis;

--
-- TOC entry 10 (class 2615 OID 17755)
-- Name: messaging; Type: SCHEMA; Schema: -; Owner: vipslogic
--

CREATE SCHEMA messaging;


ALTER SCHEMA messaging OWNER TO vipslogic;

SET search_path = messaging, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 208 (class 1259 OID 19167)
-- Name: forecast_event; Type: TABLE; Schema: messaging; Owner: vipslogic
--

CREATE TABLE forecast_event (
    forecast_event_id integer NOT NULL,
    event_name character varying(255),
    event_description text
);


ALTER TABLE forecast_event OWNER TO vipslogic;

--
-- TOC entry 209 (class 1259 OID 19173)
-- Name: forecast_event_notification_subscription; Type: TABLE; Schema: messaging; Owner: vipslogic
--

CREATE TABLE forecast_event_notification_subscription (
    user_id integer NOT NULL,
    universal_message_format_id integer,
    weather_station_ids integer[],
    crop_category_ids integer[]
);


ALTER TABLE forecast_event_notification_subscription OWNER TO vipslogic;

--
-- TOC entry 210 (class 1259 OID 19179)
-- Name: forecast_notification_log; Type: TABLE; Schema: messaging; Owner: vipslogic
--

CREATE TABLE forecast_notification_log (
    forecast_configuration_id integer NOT NULL,
    universal_message_id integer,
    forecast_event_id integer NOT NULL,
    event_date date NOT NULL,
    created_time timestamp with time zone
);


ALTER TABLE forecast_notification_log OWNER TO vipslogic;

--
-- TOC entry 211 (class 1259 OID 19182)
-- Name: message_notification_subscription; Type: TABLE; Schema: messaging; Owner: vipslogic
--

CREATE TABLE message_notification_subscription (
    user_id integer NOT NULL,
    universal_message_format_id integer,
    message_tag_ids integer[],
    crop_category_ids integer[]
);


ALTER TABLE message_notification_subscription OWNER TO vipslogic;

--
-- TOC entry 212 (class 1259 OID 19188)
-- Name: observation_notification_subscription; Type: TABLE; Schema: messaging; Owner: vipslogic
--

CREATE TABLE observation_notification_subscription (
    user_id integer NOT NULL,
    universal_message_format_id integer,
    crop_category_ids integer[]
);


ALTER TABLE observation_notification_subscription OWNER TO vipslogic;

--
-- TOC entry 213 (class 1259 OID 19194)
-- Name: universal_message; Type: TABLE; Schema: messaging; Owner: vipslogic
--

CREATE TABLE universal_message (
    universal_message_id integer NOT NULL,
    message_local_versions json,
    expires_at timestamp with time zone,
    distribution_list json
);


ALTER TABLE universal_message OWNER TO vipslogic;

--
-- TOC entry 214 (class 1259 OID 19200)
-- Name: universal_message_format; Type: TABLE; Schema: messaging; Owner: vipslogic
--

CREATE TABLE universal_message_format (
    universal_message_format_id integer NOT NULL,
    format_name character varying(63)
);


ALTER TABLE universal_message_format OWNER TO vipslogic;

--
-- TOC entry 215 (class 1259 OID 19203)
-- Name: universal_message_universal_message_id_seq; Type: SEQUENCE; Schema: messaging; Owner: vipslogic
--

CREATE SEQUENCE universal_message_universal_message_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE universal_message_universal_message_id_seq OWNER TO vipslogic;

--
-- TOC entry 4115 (class 0 OID 0)
-- Dependencies: 215
-- Name: universal_message_universal_message_id_seq; Type: SEQUENCE OWNED BY; Schema: messaging; Owner: vipslogic
--

ALTER SEQUENCE universal_message_universal_message_id_seq OWNED BY universal_message.universal_message_id;


SET search_path = public, pg_catalog;

--
-- TOC entry 216 (class 1259 OID 19205)
-- Name: country; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE country (
    country_code character varying(2) NOT NULL,
    phone_code character varying(10)
);


ALTER TABLE country OWNER TO vipslogic;

--
-- TOC entry 217 (class 1259 OID 19208)
-- Name: crop_category; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE crop_category (
    crop_category_id integer NOT NULL,
    default_name character varying(255),
    crop_organism_ids integer[],
    organization_id integer,
    max_hierarchy_category_id integer DEFAULT 121
);


ALTER TABLE crop_category OWNER TO vipslogic;

--
-- TOC entry 218 (class 1259 OID 19214)
-- Name: crop_category_local; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE crop_category_local (
    crop_category_id integer NOT NULL,
    locale character varying(10) NOT NULL,
    local_name character varying(255)
);


ALTER TABLE crop_category_local OWNER TO vipslogic;

--
-- TOC entry 219 (class 1259 OID 19217)
-- Name: crop_pest; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE crop_pest (
    crop_organism_id integer NOT NULL,
    pest_organism_ids integer[],
    include_all_child_crops boolean DEFAULT false
);


ALTER TABLE crop_pest OWNER TO vipslogic;

--
-- TOC entry 220 (class 1259 OID 19224)
-- Name: currency; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE currency (
    currency_id character varying(10) NOT NULL,
    currency_name character varying(255)
);


ALTER TABLE currency OWNER TO vipslogic;

--
-- TOC entry 221 (class 1259 OID 19227)
-- Name: external_resource; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE external_resource (
    external_resource_id integer NOT NULL,
    external_resource_type_id integer,
    uri character varying(1023),
    identifier_template character varying(255),
    name character varying(255)
);


ALTER TABLE external_resource OWNER TO vipslogic;

--
-- TOC entry 222 (class 1259 OID 19233)
-- Name: external_resource_external_resource_id_seq; Type: SEQUENCE; Schema: public; Owner: vipslogic
--

CREATE SEQUENCE external_resource_external_resource_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE external_resource_external_resource_id_seq OWNER TO vipslogic;

--
-- TOC entry 4116 (class 0 OID 0)
-- Dependencies: 222
-- Name: external_resource_external_resource_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vipslogic
--

ALTER SEQUENCE external_resource_external_resource_id_seq OWNED BY external_resource.external_resource_id;


--
-- TOC entry 223 (class 1259 OID 19235)
-- Name: external_resource_type; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE external_resource_type (
    external_resource_type_id integer NOT NULL,
    default_name character varying(255)
);


ALTER TABLE external_resource_type OWNER TO vipslogic;

--
-- TOC entry 224 (class 1259 OID 19238)
-- Name: external_resource_validity; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE external_resource_validity (
    external_resource_id integer NOT NULL,
    country_code character varying(2) NOT NULL
);


ALTER TABLE external_resource_validity OWNER TO vipslogic;

--
-- TOC entry 225 (class 1259 OID 19241)
-- Name: forecast_configuration; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE forecast_configuration (
    forecast_configuration_id integer NOT NULL,
    location_point_of_interest_id integer,
    model_id character(10),
    date_start date,
    date_end date,
    vips_logic_user_id integer,
    weather_station_point_of_interest_id integer,
    crop_organism_id integer,
    pest_organism_id integer,
    time_zone character varying(255),
    is_private boolean DEFAULT false
);


ALTER TABLE forecast_configuration OWNER TO vipslogic;

--
-- TOC entry 226 (class 1259 OID 19245)
-- Name: forecast_configuration_forecast_configuration_id_seq; Type: SEQUENCE; Schema: public; Owner: vipslogic
--

CREATE SEQUENCE forecast_configuration_forecast_configuration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE forecast_configuration_forecast_configuration_id_seq OWNER TO vipslogic;

--
-- TOC entry 4117 (class 0 OID 0)
-- Dependencies: 226
-- Name: forecast_configuration_forecast_configuration_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vipslogic
--

ALTER SEQUENCE forecast_configuration_forecast_configuration_id_seq OWNED BY forecast_configuration.forecast_configuration_id;


--
-- TOC entry 227 (class 1259 OID 19247)
-- Name: forecast_model_configuration; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE forecast_model_configuration (
    forecast_configuration_id integer NOT NULL,
    model_config_parameter character varying(255) NOT NULL,
    parameter_value character varying(1024)
);


ALTER TABLE forecast_model_configuration OWNER TO vipslogic;

--
-- TOC entry 228 (class 1259 OID 19253)
-- Name: forecast_result; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE forecast_result (
    forecast_result_id bigint NOT NULL,
    forecast_configuration_id integer,
    valid_time_start timestamp with time zone,
    warning_status integer,
    all_values text,
    valid_time_end timestamp with time zone,
    valid_geometry geometry(Geometry,4326)
);


ALTER TABLE forecast_result OWNER TO vipslogic;

--
-- TOC entry 229 (class 1259 OID 19259)
-- Name: forecast_result_cache; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE forecast_result_cache (
    forecast_result_id bigint NOT NULL,
    forecast_configuration_id integer,
    valid_time_start timestamp with time zone,
    warning_status integer,
    all_values text,
    valid_time_end timestamp with time zone,
    valid_geometry geometry(Geometry,4326)
);


ALTER TABLE forecast_result_cache OWNER TO vipslogic;

--
-- TOC entry 230 (class 1259 OID 19265)
-- Name: forecast_result_forecast_result_id_seq; Type: SEQUENCE; Schema: public; Owner: vipslogic
--

CREATE SEQUENCE forecast_result_forecast_result_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE forecast_result_forecast_result_id_seq OWNER TO vipslogic;

--
-- TOC entry 4118 (class 0 OID 0)
-- Dependencies: 230
-- Name: forecast_result_forecast_result_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vipslogic
--

ALTER SEQUENCE forecast_result_forecast_result_id_seq OWNED BY forecast_result.forecast_result_id;


--
-- TOC entry 231 (class 1259 OID 19267)
-- Name: forecast_summary; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE forecast_summary (
    forecast_configuration_id integer NOT NULL,
    summary_for_date date NOT NULL,
    summary_created_time timestamp with time zone DEFAULT now(),
    warning_status integer
);


ALTER TABLE forecast_summary OWNER TO vipslogic;

--
-- TOC entry 232 (class 1259 OID 19271)
-- Name: gis; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE gis (
    gis_id integer NOT NULL,
    gis_geom_2 geometry(Geometry,4326),
    gis_geom geometry(GeometryZ,4326)
);


ALTER TABLE gis OWNER TO vipslogic;

--
-- TOC entry 233 (class 1259 OID 19277)
-- Name: gis_gis_id_seq; Type: SEQUENCE; Schema: public; Owner: vipslogic
--

CREATE SEQUENCE gis_gis_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE gis_gis_id_seq OWNER TO vipslogic;

--
-- TOC entry 4119 (class 0 OID 0)
-- Dependencies: 233
-- Name: gis_gis_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vipslogic
--

ALTER SEQUENCE gis_gis_id_seq OWNED BY gis.gis_id;


--
-- TOC entry 234 (class 1259 OID 19279)
-- Name: gis_observation; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE gis_observation (
    gis_id integer NOT NULL,
    observation_id integer NOT NULL
);


ALTER TABLE gis_observation OWNER TO vipslogic;

--
-- TOC entry 235 (class 1259 OID 19282)
-- Name: hierarchy_category; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE hierarchy_category (
    hierarchy_category_id integer NOT NULL,
    default_name character varying(63),
    logically_deleted boolean DEFAULT false NOT NULL
);


ALTER TABLE hierarchy_category OWNER TO vipslogic;

--
-- TOC entry 236 (class 1259 OID 19286)
-- Name: hierarchy_category_locale; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE hierarchy_category_locale (
    hierarchy_category_id integer NOT NULL,
    locale character varying(10) NOT NULL,
    local_name character varying(255)
);


ALTER TABLE hierarchy_category_locale OWNER TO vipslogic;

--
-- TOC entry 237 (class 1259 OID 19289)
-- Name: map_layer; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE map_layer (
    map_layer_id integer NOT NULL,
    map_layer_title character varying(255),
    description text,
    map_layer_type character varying(255),
    default_visible boolean DEFAULT false,
    hover_attribute character varying(63),
    url character varying(1023)
);


ALTER TABLE map_layer OWNER TO vipslogic;

--
-- TOC entry 238 (class 1259 OID 19296)
-- Name: map_layer_map_layer_id_seq; Type: SEQUENCE; Schema: public; Owner: vipslogic
--

CREATE SEQUENCE map_layer_map_layer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE map_layer_map_layer_id_seq OWNER TO vipslogic;

--
-- TOC entry 4120 (class 0 OID 0)
-- Dependencies: 238
-- Name: map_layer_map_layer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vipslogic
--

ALTER SEQUENCE map_layer_map_layer_id_seq OWNED BY map_layer.map_layer_id;


--
-- TOC entry 239 (class 1259 OID 19298)
-- Name: message; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE message (
    message_id integer NOT NULL,
    organization_id integer,
    date_pub date DEFAULT now(),
    date_valid_to date,
    crop_category_ids integer[]
);


ALTER TABLE message OWNER TO vipslogic;

--
-- TOC entry 240 (class 1259 OID 19305)
-- Name: message_illustration; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE message_illustration (
    message_id integer NOT NULL,
    file_name character varying(2047) NOT NULL
);


ALTER TABLE message_illustration OWNER TO vipslogic;

--
-- TOC entry 241 (class 1259 OID 19311)
-- Name: message_illustration_caption_locale; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE message_illustration_caption_locale (
    message_id integer NOT NULL,
    file_name character varying(2047) NOT NULL,
    locale character varying(10) NOT NULL,
    caption text
);


ALTER TABLE message_illustration_caption_locale OWNER TO vipslogic;

--
-- TOC entry 242 (class 1259 OID 19317)
-- Name: message_locale; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE message_locale (
    message_id integer NOT NULL,
    locale character varying(10) NOT NULL,
    heading character varying(511) NOT NULL,
    lead_paragraph text,
    body text,
    created_by integer,
    created_timestamp timestamp with time zone DEFAULT now(),
    modified_timestamp timestamp with time zone
);


ALTER TABLE message_locale OWNER TO vipslogic;

--
-- TOC entry 243 (class 1259 OID 19324)
-- Name: message_message_id_seq; Type: SEQUENCE; Schema: public; Owner: vipslogic
--

CREATE SEQUENCE message_message_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE message_message_id_seq OWNER TO vipslogic;

--
-- TOC entry 4121 (class 0 OID 0)
-- Dependencies: 243
-- Name: message_message_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vipslogic
--

ALTER SEQUENCE message_message_id_seq OWNED BY message.message_id;


--
-- TOC entry 244 (class 1259 OID 19326)
-- Name: message_message_tag; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE message_message_tag (
    message_id integer NOT NULL,
    message_tag_id integer NOT NULL
);


ALTER TABLE message_message_tag OWNER TO vipslogic;

--
-- TOC entry 245 (class 1259 OID 19329)
-- Name: message_tag; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE message_tag (
    message_tag_id integer NOT NULL,
    default_tag_name character varying(255)
);


ALTER TABLE message_tag OWNER TO vipslogic;

--
-- TOC entry 246 (class 1259 OID 19332)
-- Name: message_tag_locale; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE message_tag_locale (
    message_tag_id integer NOT NULL,
    locale character varying(10) NOT NULL,
    local_name character varying(255)
);


ALTER TABLE message_tag_locale OWNER TO vipslogic;

--
-- TOC entry 247 (class 1259 OID 19335)
-- Name: message_tag_message_tag_id_seq; Type: SEQUENCE; Schema: public; Owner: vipslogic
--

CREATE SEQUENCE message_tag_message_tag_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE message_tag_message_tag_id_seq OWNER TO vipslogic;

--
-- TOC entry 4122 (class 0 OID 0)
-- Dependencies: 247
-- Name: message_tag_message_tag_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vipslogic
--

ALTER SEQUENCE message_tag_message_tag_id_seq OWNED BY message_tag.message_tag_id;


--
-- TOC entry 248 (class 1259 OID 19337)
-- Name: model_information; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE model_information (
    model_id character varying(10) NOT NULL,
    date_first_registered date,
    date_last_registered date,
    default_name character varying(255),
    copyright_holder character varying(255),
    license text,
    default_description text,
    sample_config text,
    usage text
);


ALTER TABLE model_information OWNER TO vipslogic;

--
-- TOC entry 249 (class 1259 OID 19343)
-- Name: observation; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE observation (
    observation_id integer NOT NULL,
    organism_id integer,
    time_of_observation timestamp with time zone NOT NULL,
    user_id integer,
    observation_method_id character varying(63),
    observation_heading character varying(1023),
    observation_text text,
    status_type_id integer DEFAULT 1,
    status_changed_by_user_id integer,
    status_changed_time timestamp with time zone,
    status_remarks text,
    observation_data json,
    is_quantified boolean DEFAULT false,
    crop_organism_id integer,
    broadcast_message boolean DEFAULT true,
    location_point_of_interest_id integer,
    location_is_private boolean DEFAULT false,
    last_edited_by integer
);


ALTER TABLE observation OWNER TO vipslogic;

--
-- TOC entry 250 (class 1259 OID 19352)
-- Name: observation_data_schema; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE observation_data_schema (
    organization_id integer NOT NULL,
    organism_id integer NOT NULL,
    data_schema json,
    data_model json
);


ALTER TABLE observation_data_schema OWNER TO vipslogic;

--
-- TOC entry 288 (class 1259 OID 106217)
-- Name: observation_form_shortcut; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE observation_form_shortcut (
    observation_form_shortcut_id integer NOT NULL,
    organization_id integer,
    default_label character varying(255),
    form_query_param_string character varying(2047),
    list_query_param_string character varying(2047)
);


ALTER TABLE observation_form_shortcut OWNER TO vipslogic;

--
-- TOC entry 290 (class 1259 OID 106233)
-- Name: observation_form_shortcut_locale; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE observation_form_shortcut_locale (
    observation_form_shortcut_id integer NOT NULL,
    locale character varying(10),
    label character varying(255)
);


ALTER TABLE observation_form_shortcut_locale OWNER TO vipslogic;

--
-- TOC entry 289 (class 1259 OID 106231)
-- Name: observation_form_shortcut_loca_observation_form_shortcut_id_seq; Type: SEQUENCE; Schema: public; Owner: vipslogic
--

CREATE SEQUENCE observation_form_shortcut_loca_observation_form_shortcut_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE observation_form_shortcut_loca_observation_form_shortcut_id_seq OWNER TO vipslogic;

--
-- TOC entry 4123 (class 0 OID 0)
-- Dependencies: 289
-- Name: observation_form_shortcut_loca_observation_form_shortcut_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vipslogic
--

ALTER SEQUENCE observation_form_shortcut_loca_observation_form_shortcut_id_seq OWNED BY observation_form_shortcut_locale.observation_form_shortcut_id;


--
-- TOC entry 287 (class 1259 OID 106215)
-- Name: observation_form_shortcut_observation_form_shortcut_id_seq; Type: SEQUENCE; Schema: public; Owner: vipslogic
--

CREATE SEQUENCE observation_form_shortcut_observation_form_shortcut_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE observation_form_shortcut_observation_form_shortcut_id_seq OWNER TO vipslogic;

--
-- TOC entry 4124 (class 0 OID 0)
-- Dependencies: 287
-- Name: observation_form_shortcut_observation_form_shortcut_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vipslogic
--

ALTER SEQUENCE observation_form_shortcut_observation_form_shortcut_id_seq OWNED BY observation_form_shortcut.observation_form_shortcut_id;


--
-- TOC entry 251 (class 1259 OID 19358)
-- Name: observation_illustration; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE observation_illustration (
    observation_id integer NOT NULL,
    file_name character varying(2047) NOT NULL
);


ALTER TABLE observation_illustration OWNER TO vipslogic;

--
-- TOC entry 252 (class 1259 OID 19364)
-- Name: observation_method; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE observation_method (
    observation_method_id character varying(63) NOT NULL
);


ALTER TABLE observation_method OWNER TO vipslogic;

--
-- TOC entry 253 (class 1259 OID 19367)
-- Name: observation_observation_id_seq; Type: SEQUENCE; Schema: public; Owner: vipslogic
--

CREATE SEQUENCE observation_observation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE observation_observation_id_seq OWNER TO vipslogic;

--
-- TOC entry 4125 (class 0 OID 0)
-- Dependencies: 253
-- Name: observation_observation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vipslogic
--

ALTER SEQUENCE observation_observation_id_seq OWNED BY observation.observation_id;


--
-- TOC entry 254 (class 1259 OID 19369)
-- Name: observation_status_type; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE observation_status_type (
    status_type_id integer NOT NULL,
    status_title character varying(63)
);


ALTER TABLE observation_status_type OWNER TO vipslogic;

--
-- TOC entry 255 (class 1259 OID 19372)
-- Name: organism; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE organism (
    organism_id integer NOT NULL,
    latin_name character varying(255),
    trade_name character varying(255),
    parent_organism_id integer,
    hierarchy_category_id integer,
    logically_deleted boolean DEFAULT false NOT NULL,
    is_crop boolean,
    is_pest boolean
);


ALTER TABLE organism OWNER TO vipslogic;

--
-- TOC entry 256 (class 1259 OID 19379)
-- Name: organism_external_resource; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE organism_external_resource (
    organism_id integer NOT NULL,
    external_resource_id integer NOT NULL,
    resource_identifier character varying(255)
);


ALTER TABLE organism_external_resource OWNER TO vipslogic;

--
-- TOC entry 257 (class 1259 OID 19382)
-- Name: organism_locale; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE organism_locale (
    organism_id integer NOT NULL,
    locale character varying(10) NOT NULL,
    local_name character varying(255)
);


ALTER TABLE organism_locale OWNER TO vipslogic;

--
-- TOC entry 258 (class 1259 OID 19385)
-- Name: organism_organism_id_seq; Type: SEQUENCE; Schema: public; Owner: vipslogic
--

CREATE SEQUENCE organism_organism_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE organism_organism_id_seq OWNER TO vipslogic;

--
-- TOC entry 4126 (class 0 OID 0)
-- Dependencies: 258
-- Name: organism_organism_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vipslogic
--

ALTER SEQUENCE organism_organism_id_seq OWNED BY organism.organism_id;


--
-- TOC entry 259 (class 1259 OID 19387)
-- Name: organization; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE organization (
    organization_id integer NOT NULL,
    organization_name character varying(255),
    parent_organization_id integer,
    address1 character varying(255),
    address2 character varying(255),
    postal_code character varying(63),
    country_code character varying(2),
    default_locale character varying(63),
    default_map_center geometry(Point,4326),
    default_map_zoom integer,
    default_time_zone character varying(255) DEFAULT 'UTC'::character varying,
    city character varying(255),
    default_vips_core_user_id integer,
    vipsweb_url character varying(255)
);


ALTER TABLE organization OWNER TO vipslogic;

--
-- TOC entry 260 (class 1259 OID 19394)
-- Name: organization_group; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE organization_group (
    organization_group_id integer NOT NULL,
    organization_id integer,
    group_name character varying(255)
);


ALTER TABLE organization_group OWNER TO vipslogic;

--
-- TOC entry 291 (class 1259 OID 121353)
-- Name: organization_group_observation; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE organization_group_observation (
    organization_group_id integer NOT NULL,
    observation_id integer NOT NULL
);


ALTER TABLE organization_group_observation OWNER TO vipslogic;

--
-- TOC entry 261 (class 1259 OID 19397)
-- Name: organization_group_organization_group_id_seq; Type: SEQUENCE; Schema: public; Owner: vipslogic
--

CREATE SEQUENCE organization_group_organization_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE organization_group_organization_group_id_seq OWNER TO vipslogic;

--
-- TOC entry 4127 (class 0 OID 0)
-- Dependencies: 261
-- Name: organization_group_organization_group_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vipslogic
--

ALTER SEQUENCE organization_group_organization_group_id_seq OWNED BY organization_group.organization_group_id;


--
-- TOC entry 262 (class 1259 OID 19399)
-- Name: organization_group_poi; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE organization_group_poi (
    organization_group_id integer NOT NULL,
    point_of_interest_id integer NOT NULL
);


ALTER TABLE organization_group_poi OWNER TO vipslogic;

--
-- TOC entry 263 (class 1259 OID 19402)
-- Name: organization_group_user; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE organization_group_user (
    organization_group_id integer NOT NULL,
    user_id integer NOT NULL
);


ALTER TABLE organization_group_user OWNER TO vipslogic;

--
-- TOC entry 264 (class 1259 OID 19405)
-- Name: organization_map_layer; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE organization_map_layer (
    organization_id integer NOT NULL,
    map_layer_id integer NOT NULL,
    ordering integer
);


ALTER TABLE organization_map_layer OWNER TO vipslogic;

--
-- TOC entry 265 (class 1259 OID 19408)
-- Name: organization_organization_id_seq; Type: SEQUENCE; Schema: public; Owner: vipslogic
--

CREATE SEQUENCE organization_organization_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE organization_organization_id_seq OWNER TO vipslogic;

--
-- TOC entry 4128 (class 0 OID 0)
-- Dependencies: 265
-- Name: organization_organization_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vipslogic
--

ALTER SEQUENCE organization_organization_id_seq OWNED BY organization.organization_id;


--
-- TOC entry 266 (class 1259 OID 19410)
-- Name: point_of_interest; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE point_of_interest (
    point_of_interest_id integer NOT NULL,
    name character varying(255),
    longitude double precision,
    latitude double precision,
    altitude double precision,
    country_code character varying(2),
    point_of_interest_type_id integer,
    time_zone character varying(255) DEFAULT 'UTC'::character varying,
    user_id integer,
    weather_forecast_provider_id integer,
    gis_geom geometry(GeometryZ,4326),
    is_forecast_location boolean DEFAULT false
);


ALTER TABLE point_of_interest OWNER TO vipslogic;

--
-- TOC entry 267 (class 1259 OID 19417)
-- Name: point_of_interest_external_resource; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE point_of_interest_external_resource (
    point_of_interest_id integer NOT NULL,
    external_resource_id integer NOT NULL,
    resource_identifier character varying(255)
);


ALTER TABLE point_of_interest_external_resource OWNER TO vipslogic;

--
-- TOC entry 268 (class 1259 OID 19420)
-- Name: point_of_interest_point_of_interest_id_seq; Type: SEQUENCE; Schema: public; Owner: vipslogic
--

CREATE SEQUENCE point_of_interest_point_of_interest_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE point_of_interest_point_of_interest_id_seq OWNER TO vipslogic;

--
-- TOC entry 4129 (class 0 OID 0)
-- Dependencies: 268
-- Name: point_of_interest_point_of_interest_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vipslogic
--

ALTER SEQUENCE point_of_interest_point_of_interest_id_seq OWNED BY point_of_interest.point_of_interest_id;


--
-- TOC entry 269 (class 1259 OID 19422)
-- Name: point_of_interest_type; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE point_of_interest_type (
    point_of_interest_type_id integer NOT NULL,
    default_name character varying(255)
);


ALTER TABLE point_of_interest_type OWNER TO vipslogic;

--
-- TOC entry 270 (class 1259 OID 19425)
-- Name: point_of_interest_weather_station; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE point_of_interest_weather_station (
    point_of_interest_id integer NOT NULL,
    weather_station_data_source_id integer,
    weather_station_remote_id character varying(255),
    active boolean DEFAULT true
);


ALTER TABLE point_of_interest_weather_station OWNER TO vipslogic;

--
-- TOC entry 271 (class 1259 OID 19429)
-- Name: preparation; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE preparation (
    preparation_id integer NOT NULL,
    organization_id integer,
    preparation_name character varying(255),
    preparation_type_id integer,
    price numeric,
    currency_id character varying(10)
);


ALTER TABLE preparation OWNER TO vipslogic;

--
-- TOC entry 272 (class 1259 OID 19435)
-- Name: preparation_preparation_id_seq; Type: SEQUENCE; Schema: public; Owner: vipslogic
--

CREATE SEQUENCE preparation_preparation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE preparation_preparation_id_seq OWNER TO vipslogic;

--
-- TOC entry 4130 (class 0 OID 0)
-- Dependencies: 272
-- Name: preparation_preparation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vipslogic
--

ALTER SEQUENCE preparation_preparation_id_seq OWNED BY preparation.preparation_id;


--
-- TOC entry 273 (class 1259 OID 19437)
-- Name: preparation_type; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE preparation_type (
    preparation_type_id integer NOT NULL,
    preparation_type_name character varying(255)
);


ALTER TABLE preparation_type OWNER TO vipslogic;

--
-- TOC entry 274 (class 1259 OID 19440)
-- Name: task_history; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE task_history (
    task_history_id integer NOT NULL,
    task_factory_id integer,
    start_time timestamp with time zone,
    finish_time timestamp with time zone,
    completeness_at_finish numeric,
    task_history_status_id character varying(127),
    message text,
    organization_id integer
);


ALTER TABLE task_history OWNER TO vipslogic;

--
-- TOC entry 275 (class 1259 OID 19446)
-- Name: task_history_status; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE task_history_status (
    task_history_status_id character varying(127) NOT NULL,
    default_title character varying(63)
);


ALTER TABLE task_history_status OWNER TO vipslogic;

--
-- TOC entry 276 (class 1259 OID 19449)
-- Name: task_history_task_history_id_seq; Type: SEQUENCE; Schema: public; Owner: vipslogic
--

CREATE SEQUENCE task_history_task_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE task_history_task_history_id_seq OWNER TO vipslogic;

--
-- TOC entry 4131 (class 0 OID 0)
-- Dependencies: 276
-- Name: task_history_task_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vipslogic
--

ALTER SEQUENCE task_history_task_history_id_seq OWNED BY task_history.task_history_id;


--
-- TOC entry 277 (class 1259 OID 19451)
-- Name: user_authentication; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE user_authentication (
    user_id integer NOT NULL,
    user_authentication_type_id integer NOT NULL,
    username character varying(2047),
    password character varying(63),
    password_reset_code character varying(255),
    password_reset_code_creation_time timestamp with time zone
);


ALTER TABLE user_authentication OWNER TO vipslogic;

--
-- TOC entry 278 (class 1259 OID 19457)
-- Name: user_authentication_type; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE user_authentication_type (
    user_authentication_type_id integer NOT NULL,
    name character varying(63),
    description character varying(1023)
);


ALTER TABLE user_authentication_type OWNER TO vipslogic;

--
-- TOC entry 279 (class 1259 OID 19463)
-- Name: user_uuid; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE user_uuid (
    user_uuid uuid NOT NULL,
    user_id integer NOT NULL,
    expires_at timestamp with time zone NOT NULL
);


ALTER TABLE user_uuid OWNER TO vipslogic;

--
-- TOC entry 280 (class 1259 OID 19466)
-- Name: user_vips_logic_role; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE user_vips_logic_role (
    vips_logic_role_id integer NOT NULL,
    user_id integer NOT NULL
);


ALTER TABLE user_vips_logic_role OWNER TO vipslogic;

--
-- TOC entry 281 (class 1259 OID 19469)
-- Name: vips_logic_role; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE vips_logic_role (
    vips_logic_role_id integer NOT NULL,
    default_title character varying(255),
    default_description text
);


ALTER TABLE vips_logic_role OWNER TO vipslogic;

--
-- TOC entry 282 (class 1259 OID 19475)
-- Name: vips_logic_user; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE vips_logic_user (
    user_id integer NOT NULL,
    email character varying(1023),
    first_name character varying(255),
    last_name character varying(255),
    organization_id integer,
    approval_application character varying(2047),
    remarks text,
    user_status_id integer,
    vips_core_user_id integer,
    email_verification_code character varying(255) DEFAULT NULL::character varying,
    preferred_locale character varying(10) DEFAULT 'en'::character varying NOT NULL,
    phone character varying(63),
    approves_sms_billing boolean DEFAULT false,
    phone_country_code character varying(10) DEFAULT '47'::character varying NOT NULL,
    free_sms boolean DEFAULT false
);


ALTER TABLE vips_logic_user OWNER TO vipslogic;

--
-- TOC entry 283 (class 1259 OID 19486)
-- Name: vips_logic_user_user_id_seq; Type: SEQUENCE; Schema: public; Owner: vipslogic
--

CREATE SEQUENCE vips_logic_user_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE vips_logic_user_user_id_seq OWNER TO vipslogic;

--
-- TOC entry 4132 (class 0 OID 0)
-- Dependencies: 283
-- Name: vips_logic_user_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vipslogic
--

ALTER SEQUENCE vips_logic_user_user_id_seq OWNED BY vips_logic_user.user_id;


--
-- TOC entry 284 (class 1259 OID 19488)
-- Name: weather_forecast_provider; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE weather_forecast_provider (
    weather_forecast_provider_id integer NOT NULL,
    name character varying(255),
    info_url character varying(511),
    description text
);


ALTER TABLE weather_forecast_provider OWNER TO vipslogic;

--
-- TOC entry 285 (class 1259 OID 19494)
-- Name: weather_station_data_source; Type: TABLE; Schema: public; Owner: vipslogic
--

CREATE TABLE weather_station_data_source (
    weather_station_data_source_id integer NOT NULL,
    name character varying(255),
    default_description text,
    uri character varying(1024),
    datafetch_uri_expression character varying(1024),
    info_uri_expression character varying(1024) DEFAULT ''::character varying
);


ALTER TABLE weather_station_data_source OWNER TO vipslogic;

--
-- TOC entry 286 (class 1259 OID 19501)
-- Name: weather_station_data_source_weather_station_data_source_id_seq; Type: SEQUENCE; Schema: public; Owner: vipslogic
--

CREATE SEQUENCE weather_station_data_source_weather_station_data_source_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE weather_station_data_source_weather_station_data_source_id_seq OWNER TO vipslogic;

--
-- TOC entry 4133 (class 0 OID 0)
-- Dependencies: 286
-- Name: weather_station_data_source_weather_station_data_source_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vipslogic
--

ALTER SEQUENCE weather_station_data_source_weather_station_data_source_id_seq OWNED BY weather_station_data_source.weather_station_data_source_id;


SET search_path = messaging, pg_catalog;

--
-- TOC entry 3653 (class 2604 OID 19505)
-- Name: universal_message_id; Type: DEFAULT; Schema: messaging; Owner: vipslogic
--

ALTER TABLE ONLY universal_message ALTER COLUMN universal_message_id SET DEFAULT nextval('universal_message_universal_message_id_seq'::regclass);


SET search_path = public, pg_catalog;

--
-- TOC entry 3656 (class 2604 OID 19506)
-- Name: external_resource_id; Type: DEFAULT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY external_resource ALTER COLUMN external_resource_id SET DEFAULT nextval('external_resource_external_resource_id_seq'::regclass);


--
-- TOC entry 3658 (class 2604 OID 19507)
-- Name: forecast_configuration_id; Type: DEFAULT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY forecast_configuration ALTER COLUMN forecast_configuration_id SET DEFAULT nextval('forecast_configuration_forecast_configuration_id_seq'::regclass);


--
-- TOC entry 3659 (class 2604 OID 174573)
-- Name: forecast_result_id; Type: DEFAULT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY forecast_result ALTER COLUMN forecast_result_id SET DEFAULT nextval('forecast_result_forecast_result_id_seq'::regclass);


--
-- TOC entry 3661 (class 2604 OID 19509)
-- Name: gis_id; Type: DEFAULT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY gis ALTER COLUMN gis_id SET DEFAULT nextval('gis_gis_id_seq'::regclass);


--
-- TOC entry 3664 (class 2604 OID 19510)
-- Name: map_layer_id; Type: DEFAULT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY map_layer ALTER COLUMN map_layer_id SET DEFAULT nextval('map_layer_map_layer_id_seq'::regclass);


--
-- TOC entry 3666 (class 2604 OID 19511)
-- Name: message_id; Type: DEFAULT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY message ALTER COLUMN message_id SET DEFAULT nextval('message_message_id_seq'::regclass);


--
-- TOC entry 3668 (class 2604 OID 19512)
-- Name: message_tag_id; Type: DEFAULT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY message_tag ALTER COLUMN message_tag_id SET DEFAULT nextval('message_tag_message_tag_id_seq'::regclass);


--
-- TOC entry 3672 (class 2604 OID 19513)
-- Name: observation_id; Type: DEFAULT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation ALTER COLUMN observation_id SET DEFAULT nextval('observation_observation_id_seq'::regclass);


--
-- TOC entry 3693 (class 2604 OID 106220)
-- Name: observation_form_shortcut_id; Type: DEFAULT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation_form_shortcut ALTER COLUMN observation_form_shortcut_id SET DEFAULT nextval('observation_form_shortcut_observation_form_shortcut_id_seq'::regclass);


--
-- TOC entry 3694 (class 2604 OID 106236)
-- Name: observation_form_shortcut_id; Type: DEFAULT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation_form_shortcut_locale ALTER COLUMN observation_form_shortcut_id SET DEFAULT nextval('observation_form_shortcut_loca_observation_form_shortcut_id_seq'::regclass);


--
-- TOC entry 3675 (class 2604 OID 19514)
-- Name: organism_id; Type: DEFAULT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organism ALTER COLUMN organism_id SET DEFAULT nextval('organism_organism_id_seq'::regclass);


--
-- TOC entry 3677 (class 2604 OID 19515)
-- Name: organization_id; Type: DEFAULT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization ALTER COLUMN organization_id SET DEFAULT nextval('organization_organization_id_seq'::regclass);


--
-- TOC entry 3678 (class 2604 OID 19516)
-- Name: organization_group_id; Type: DEFAULT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization_group ALTER COLUMN organization_group_id SET DEFAULT nextval('organization_group_organization_group_id_seq'::regclass);


--
-- TOC entry 3680 (class 2604 OID 19517)
-- Name: point_of_interest_id; Type: DEFAULT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY point_of_interest ALTER COLUMN point_of_interest_id SET DEFAULT nextval('point_of_interest_point_of_interest_id_seq'::regclass);


--
-- TOC entry 3683 (class 2604 OID 19518)
-- Name: preparation_id; Type: DEFAULT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY preparation ALTER COLUMN preparation_id SET DEFAULT nextval('preparation_preparation_id_seq'::regclass);


--
-- TOC entry 3684 (class 2604 OID 19519)
-- Name: task_history_id; Type: DEFAULT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY task_history ALTER COLUMN task_history_id SET DEFAULT nextval('task_history_task_history_id_seq'::regclass);


--
-- TOC entry 3690 (class 2604 OID 19520)
-- Name: user_id; Type: DEFAULT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY vips_logic_user ALTER COLUMN user_id SET DEFAULT nextval('vips_logic_user_user_id_seq'::regclass);


--
-- TOC entry 3692 (class 2604 OID 19521)
-- Name: weather_station_data_source_id; Type: DEFAULT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY weather_station_data_source ALTER COLUMN weather_station_data_source_id SET DEFAULT nextval('weather_station_data_source_weather_station_data_source_id_seq'::regclass);


SET search_path = messaging, pg_catalog;

--
-- TOC entry 3698 (class 2606 OID 32443)
-- Name: forecast_event_notification_subscription_pkey; Type: CONSTRAINT; Schema: messaging; Owner: vipslogic
--

ALTER TABLE ONLY forecast_event_notification_subscription
    ADD CONSTRAINT forecast_event_notification_subscription_pkey PRIMARY KEY (user_id);


--
-- TOC entry 3696 (class 2606 OID 32445)
-- Name: forecast_event_pkey; Type: CONSTRAINT; Schema: messaging; Owner: vipslogic
--

ALTER TABLE ONLY forecast_event
    ADD CONSTRAINT forecast_event_pkey PRIMARY KEY (forecast_event_id);


--
-- TOC entry 3700 (class 2606 OID 32447)
-- Name: forecast_notification_log_pk; Type: CONSTRAINT; Schema: messaging; Owner: vipslogic
--

ALTER TABLE ONLY forecast_notification_log
    ADD CONSTRAINT forecast_notification_log_pk PRIMARY KEY (forecast_configuration_id, forecast_event_id, event_date);


--
-- TOC entry 3702 (class 2606 OID 32449)
-- Name: message_notification_subscription_pkey; Type: CONSTRAINT; Schema: messaging; Owner: vipslogic
--

ALTER TABLE ONLY message_notification_subscription
    ADD CONSTRAINT message_notification_subscription_pkey PRIMARY KEY (user_id);


--
-- TOC entry 3704 (class 2606 OID 32451)
-- Name: observation_notification_subscription_pkey; Type: CONSTRAINT; Schema: messaging; Owner: vipslogic
--

ALTER TABLE ONLY observation_notification_subscription
    ADD CONSTRAINT observation_notification_subscription_pkey PRIMARY KEY (user_id);


--
-- TOC entry 3708 (class 2606 OID 32453)
-- Name: universal_message_format_pkey; Type: CONSTRAINT; Schema: messaging; Owner: vipslogic
--

ALTER TABLE ONLY universal_message_format
    ADD CONSTRAINT universal_message_format_pkey PRIMARY KEY (universal_message_format_id);


--
-- TOC entry 3706 (class 2606 OID 32455)
-- Name: universal_message_pkey; Type: CONSTRAINT; Schema: messaging; Owner: vipslogic
--

ALTER TABLE ONLY universal_message
    ADD CONSTRAINT universal_message_pkey PRIMARY KEY (universal_message_id);


SET search_path = public, pg_catalog;

--
-- TOC entry 3710 (class 2606 OID 32457)
-- Name: country_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY country
    ADD CONSTRAINT country_pkey PRIMARY KEY (country_code);


--
-- TOC entry 3714 (class 2606 OID 32459)
-- Name: crop_category_local_pk; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY crop_category_local
    ADD CONSTRAINT crop_category_local_pk PRIMARY KEY (crop_category_id, locale);


--
-- TOC entry 3712 (class 2606 OID 32461)
-- Name: crop_category_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY crop_category
    ADD CONSTRAINT crop_category_pkey PRIMARY KEY (crop_category_id);


--
-- TOC entry 3716 (class 2606 OID 32463)
-- Name: crop_pests_pk; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY crop_pest
    ADD CONSTRAINT crop_pests_pk PRIMARY KEY (crop_organism_id);


--
-- TOC entry 3718 (class 2606 OID 32465)
-- Name: currency_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY currency
    ADD CONSTRAINT currency_pkey PRIMARY KEY (currency_id);


--
-- TOC entry 3720 (class 2606 OID 32467)
-- Name: external_resource_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY external_resource
    ADD CONSTRAINT external_resource_pkey PRIMARY KEY (external_resource_id);


--
-- TOC entry 3722 (class 2606 OID 32469)
-- Name: external_resource_type_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY external_resource_type
    ADD CONSTRAINT external_resource_type_pkey PRIMARY KEY (external_resource_type_id);


--
-- TOC entry 3724 (class 2606 OID 32471)
-- Name: external_resource_validity_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY external_resource_validity
    ADD CONSTRAINT external_resource_validity_pkey PRIMARY KEY (external_resource_id, country_code);


--
-- TOC entry 3726 (class 2606 OID 32473)
-- Name: forecast_configuration_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY forecast_configuration
    ADD CONSTRAINT forecast_configuration_pkey PRIMARY KEY (forecast_configuration_id);


--
-- TOC entry 3728 (class 2606 OID 32475)
-- Name: forecast_model_configuration_pk; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY forecast_model_configuration
    ADD CONSTRAINT forecast_model_configuration_pk PRIMARY KEY (forecast_configuration_id, model_config_parameter);


--
-- TOC entry 3732 (class 2606 OID 176904)
-- Name: forecast_result_cache_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY forecast_result_cache
    ADD CONSTRAINT forecast_result_cache_pkey PRIMARY KEY (forecast_result_id);


--
-- TOC entry 3730 (class 2606 OID 174575)
-- Name: forecast_result_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY forecast_result
    ADD CONSTRAINT forecast_result_pkey PRIMARY KEY (forecast_result_id);


--
-- TOC entry 3734 (class 2606 OID 32481)
-- Name: forecast_summary_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY forecast_summary
    ADD CONSTRAINT forecast_summary_pkey PRIMARY KEY (forecast_configuration_id, summary_for_date);


--
-- TOC entry 3738 (class 2606 OID 32483)
-- Name: gis_observation_pk; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY gis_observation
    ADD CONSTRAINT gis_observation_pk PRIMARY KEY (gis_id, observation_id);


--
-- TOC entry 3736 (class 2606 OID 32485)
-- Name: gis_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY gis
    ADD CONSTRAINT gis_pkey PRIMARY KEY (gis_id);


--
-- TOC entry 3742 (class 2606 OID 32487)
-- Name: hierarchy_category_locale_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY hierarchy_category_locale
    ADD CONSTRAINT hierarchy_category_locale_pkey PRIMARY KEY (hierarchy_category_id, locale);


--
-- TOC entry 3740 (class 2606 OID 32489)
-- Name: hierarchy_category_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY hierarchy_category
    ADD CONSTRAINT hierarchy_category_pkey PRIMARY KEY (hierarchy_category_id);


--
-- TOC entry 3744 (class 2606 OID 32491)
-- Name: map_layer_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY map_layer
    ADD CONSTRAINT map_layer_pkey PRIMARY KEY (map_layer_id);


--
-- TOC entry 3750 (class 2606 OID 32493)
-- Name: message_illustration_caption_locale_ok; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY message_illustration_caption_locale
    ADD CONSTRAINT message_illustration_caption_locale_ok PRIMARY KEY (message_id, file_name, locale);


--
-- TOC entry 3748 (class 2606 OID 32495)
-- Name: message_illustration_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY message_illustration
    ADD CONSTRAINT message_illustration_pkey PRIMARY KEY (message_id, file_name);


--
-- TOC entry 3752 (class 2606 OID 32497)
-- Name: message_locale_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY message_locale
    ADD CONSTRAINT message_locale_pkey PRIMARY KEY (message_id, locale);


--
-- TOC entry 3754 (class 2606 OID 32499)
-- Name: message_message_tag_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY message_message_tag
    ADD CONSTRAINT message_message_tag_pkey PRIMARY KEY (message_id, message_tag_id);


--
-- TOC entry 3746 (class 2606 OID 32501)
-- Name: message_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY message
    ADD CONSTRAINT message_pkey PRIMARY KEY (message_id);


--
-- TOC entry 3758 (class 2606 OID 32503)
-- Name: message_tag_locale_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY message_tag_locale
    ADD CONSTRAINT message_tag_locale_pkey PRIMARY KEY (message_tag_id, locale);


--
-- TOC entry 3756 (class 2606 OID 32505)
-- Name: message_tag_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY message_tag
    ADD CONSTRAINT message_tag_pkey PRIMARY KEY (message_tag_id);


--
-- TOC entry 3760 (class 2606 OID 32507)
-- Name: model_information_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY model_information
    ADD CONSTRAINT model_information_pkey PRIMARY KEY (model_id);


--
-- TOC entry 3764 (class 2606 OID 32509)
-- Name: observation_data_schema_pk; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation_data_schema
    ADD CONSTRAINT observation_data_schema_pk PRIMARY KEY (organization_id, organism_id);


--
-- TOC entry 3822 (class 2606 OID 106238)
-- Name: observation_form_shortcut_locale_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation_form_shortcut_locale
    ADD CONSTRAINT observation_form_shortcut_locale_pkey PRIMARY KEY (observation_form_shortcut_id);


--
-- TOC entry 3820 (class 2606 OID 106225)
-- Name: observation_form_shortcut_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation_form_shortcut
    ADD CONSTRAINT observation_form_shortcut_pkey PRIMARY KEY (observation_form_shortcut_id);


--
-- TOC entry 3766 (class 2606 OID 32511)
-- Name: observation_illustration_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation_illustration
    ADD CONSTRAINT observation_illustration_pkey PRIMARY KEY (observation_id, file_name);


--
-- TOC entry 3768 (class 2606 OID 32513)
-- Name: observation_method_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation_method
    ADD CONSTRAINT observation_method_pkey PRIMARY KEY (observation_method_id);


--
-- TOC entry 3762 (class 2606 OID 32515)
-- Name: observation_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation
    ADD CONSTRAINT observation_pkey PRIMARY KEY (observation_id);


--
-- TOC entry 3770 (class 2606 OID 32517)
-- Name: observation_status_type_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation_status_type
    ADD CONSTRAINT observation_status_type_pkey PRIMARY KEY (status_type_id);


--
-- TOC entry 3774 (class 2606 OID 32519)
-- Name: organism_external_resource_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organism_external_resource
    ADD CONSTRAINT organism_external_resource_pkey PRIMARY KEY (organism_id, external_resource_id);


--
-- TOC entry 3776 (class 2606 OID 32521)
-- Name: organism_locale_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organism_locale
    ADD CONSTRAINT organism_locale_pkey PRIMARY KEY (organism_id, locale);


--
-- TOC entry 3772 (class 2606 OID 32523)
-- Name: organism_node_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organism
    ADD CONSTRAINT organism_node_pkey PRIMARY KEY (organism_id);


--
-- TOC entry 3824 (class 2606 OID 121357)
-- Name: organization_group_observation_pk; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization_group_observation
    ADD CONSTRAINT organization_group_observation_pk PRIMARY KEY (organization_group_id, observation_id);


--
-- TOC entry 3780 (class 2606 OID 32525)
-- Name: organization_group_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization_group
    ADD CONSTRAINT organization_group_pkey PRIMARY KEY (organization_group_id);


--
-- TOC entry 3782 (class 2606 OID 32527)
-- Name: organization_group_poi_pk; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization_group_poi
    ADD CONSTRAINT organization_group_poi_pk PRIMARY KEY (organization_group_id, point_of_interest_id);


--
-- TOC entry 3784 (class 2606 OID 32529)
-- Name: organization_group_user_pk; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization_group_user
    ADD CONSTRAINT organization_group_user_pk PRIMARY KEY (organization_group_id, user_id);


--
-- TOC entry 3786 (class 2606 OID 32531)
-- Name: organization_map_layer_pk; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization_map_layer
    ADD CONSTRAINT organization_map_layer_pk PRIMARY KEY (organization_id, map_layer_id);


--
-- TOC entry 3778 (class 2606 OID 32533)
-- Name: organization_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization
    ADD CONSTRAINT organization_pkey PRIMARY KEY (organization_id);


--
-- TOC entry 3790 (class 2606 OID 32535)
-- Name: point_of_interest_external_resource_pk; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY point_of_interest_external_resource
    ADD CONSTRAINT point_of_interest_external_resource_pk PRIMARY KEY (point_of_interest_id, external_resource_id);


--
-- TOC entry 3788 (class 2606 OID 32537)
-- Name: point_of_interest_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY point_of_interest
    ADD CONSTRAINT point_of_interest_pkey PRIMARY KEY (point_of_interest_id);


--
-- TOC entry 3792 (class 2606 OID 32539)
-- Name: point_of_interest_type_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY point_of_interest_type
    ADD CONSTRAINT point_of_interest_type_pkey PRIMARY KEY (point_of_interest_type_id);


--
-- TOC entry 3794 (class 2606 OID 32541)
-- Name: point_of_interest_weather_station_pk; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY point_of_interest_weather_station
    ADD CONSTRAINT point_of_interest_weather_station_pk PRIMARY KEY (point_of_interest_id);


--
-- TOC entry 3796 (class 2606 OID 32543)
-- Name: preparation_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY preparation
    ADD CONSTRAINT preparation_pkey PRIMARY KEY (preparation_id);


--
-- TOC entry 3798 (class 2606 OID 32545)
-- Name: preparation_type_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY preparation_type
    ADD CONSTRAINT preparation_type_pkey PRIMARY KEY (preparation_type_id);


--
-- TOC entry 3800 (class 2606 OID 32547)
-- Name: task_history_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY task_history
    ADD CONSTRAINT task_history_pkey PRIMARY KEY (task_history_id);


--
-- TOC entry 3802 (class 2606 OID 32549)
-- Name: task_history_status_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY task_history_status
    ADD CONSTRAINT task_history_status_pkey PRIMARY KEY (task_history_status_id);


--
-- TOC entry 3804 (class 2606 OID 32551)
-- Name: user_authentication_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY user_authentication
    ADD CONSTRAINT user_authentication_pkey PRIMARY KEY (user_id, user_authentication_type_id);


--
-- TOC entry 3806 (class 2606 OID 32553)
-- Name: user_authentication_type_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY user_authentication_type
    ADD CONSTRAINT user_authentication_type_pkey PRIMARY KEY (user_authentication_type_id);


--
-- TOC entry 3808 (class 2606 OID 32555)
-- Name: user_uuid_pk; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY user_uuid
    ADD CONSTRAINT user_uuid_pk PRIMARY KEY (user_uuid, user_id);


--
-- TOC entry 3810 (class 2606 OID 32557)
-- Name: user_vips_logic_role_pk; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY user_vips_logic_role
    ADD CONSTRAINT user_vips_logic_role_pk PRIMARY KEY (vips_logic_role_id, user_id);


--
-- TOC entry 3812 (class 2606 OID 32559)
-- Name: vips_logic_role_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY vips_logic_role
    ADD CONSTRAINT vips_logic_role_pkey PRIMARY KEY (vips_logic_role_id);


--
-- TOC entry 3814 (class 2606 OID 32561)
-- Name: vips_logic_user_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY vips_logic_user
    ADD CONSTRAINT vips_logic_user_pkey PRIMARY KEY (user_id);


--
-- TOC entry 3816 (class 2606 OID 32563)
-- Name: weather_forecast_provider_pkey; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY weather_forecast_provider
    ADD CONSTRAINT weather_forecast_provider_pkey PRIMARY KEY (weather_forecast_provider_id);


--
-- TOC entry 3818 (class 2606 OID 32565)
-- Name: weather_station_data_source_pk; Type: CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY weather_station_data_source
    ADD CONSTRAINT weather_station_data_source_pk PRIMARY KEY (weather_station_data_source_id);


SET search_path = messaging, pg_catalog;

--
-- TOC entry 3825 (class 2606 OID 32601)
-- Name: forecast_event_notification_subscription_user_id_fkey; Type: FK CONSTRAINT; Schema: messaging; Owner: vipslogic
--

ALTER TABLE ONLY forecast_event_notification_subscription
    ADD CONSTRAINT forecast_event_notification_subscription_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.vips_logic_user(user_id);


--
-- TOC entry 3826 (class 2606 OID 32606)
-- Name: forecast_notification_log_forecast_configuration_id_fkey; Type: FK CONSTRAINT; Schema: messaging; Owner: vipslogic
--

ALTER TABLE ONLY forecast_notification_log
    ADD CONSTRAINT forecast_notification_log_forecast_configuration_id_fkey FOREIGN KEY (forecast_configuration_id) REFERENCES public.forecast_configuration(forecast_configuration_id);


--
-- TOC entry 3827 (class 2606 OID 32611)
-- Name: forecast_notification_log_forecast_event_id_fkey; Type: FK CONSTRAINT; Schema: messaging; Owner: vipslogic
--

ALTER TABLE ONLY forecast_notification_log
    ADD CONSTRAINT forecast_notification_log_forecast_event_id_fkey FOREIGN KEY (forecast_event_id) REFERENCES forecast_event(forecast_event_id);


--
-- TOC entry 3828 (class 2606 OID 32616)
-- Name: forecast_notification_log_universal_message_id_fkey; Type: FK CONSTRAINT; Schema: messaging; Owner: vipslogic
--

ALTER TABLE ONLY forecast_notification_log
    ADD CONSTRAINT forecast_notification_log_universal_message_id_fkey FOREIGN KEY (universal_message_id) REFERENCES universal_message(universal_message_id);


--
-- TOC entry 3829 (class 2606 OID 32621)
-- Name: message_notification_subscript_universal_message_format_id_fkey; Type: FK CONSTRAINT; Schema: messaging; Owner: vipslogic
--

ALTER TABLE ONLY message_notification_subscription
    ADD CONSTRAINT message_notification_subscript_universal_message_format_id_fkey FOREIGN KEY (universal_message_format_id) REFERENCES universal_message_format(universal_message_format_id);


--
-- TOC entry 3830 (class 2606 OID 32626)
-- Name: message_notification_subscription_user_id_fkey; Type: FK CONSTRAINT; Schema: messaging; Owner: vipslogic
--

ALTER TABLE ONLY message_notification_subscription
    ADD CONSTRAINT message_notification_subscription_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.vips_logic_user(user_id);


--
-- TOC entry 3831 (class 2606 OID 32631)
-- Name: observation_notification_subsc_universal_message_format_id_fkey; Type: FK CONSTRAINT; Schema: messaging; Owner: vipslogic
--

ALTER TABLE ONLY observation_notification_subscription
    ADD CONSTRAINT observation_notification_subsc_universal_message_format_id_fkey FOREIGN KEY (universal_message_format_id) REFERENCES universal_message_format(universal_message_format_id);


--
-- TOC entry 3832 (class 2606 OID 32636)
-- Name: observation_notification_subscription_user_id_fkey; Type: FK CONSTRAINT; Schema: messaging; Owner: vipslogic
--

ALTER TABLE ONLY observation_notification_subscription
    ADD CONSTRAINT observation_notification_subscription_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.vips_logic_user(user_id);


SET search_path = public, pg_catalog;

--
-- TOC entry 3834 (class 2606 OID 32641)
-- Name: crop_category_local_crop_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY crop_category_local
    ADD CONSTRAINT crop_category_local_crop_category_id_fkey FOREIGN KEY (crop_category_id) REFERENCES crop_category(crop_category_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3833 (class 2606 OID 32646)
-- Name: crop_category_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY crop_category
    ADD CONSTRAINT crop_category_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES organization(organization_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3835 (class 2606 OID 32651)
-- Name: crop_pest_crop_organism_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY crop_pest
    ADD CONSTRAINT crop_pest_crop_organism_id_fkey FOREIGN KEY (crop_organism_id) REFERENCES organism(organism_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3837 (class 2606 OID 32656)
-- Name: external_resource_validity_fkey_1; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY external_resource_validity
    ADD CONSTRAINT external_resource_validity_fkey_1 FOREIGN KEY (external_resource_id) REFERENCES external_resource(external_resource_id);


--
-- TOC entry 3838 (class 2606 OID 32661)
-- Name: external_resource_validity_fkey_2; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY external_resource_validity
    ADD CONSTRAINT external_resource_validity_fkey_2 FOREIGN KEY (country_code) REFERENCES country(country_code);


--
-- TOC entry 3836 (class 2606 OID 32666)
-- Name: fk_external_resource_1; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY external_resource
    ADD CONSTRAINT fk_external_resource_1 FOREIGN KEY (external_resource_type_id) REFERENCES external_resource_type(external_resource_type_id);


--
-- TOC entry 3848 (class 2606 OID 32671)
-- Name: fk_hierarchy_category_local_1; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY hierarchy_category_locale
    ADD CONSTRAINT fk_hierarchy_category_local_1 FOREIGN KEY (hierarchy_category_id) REFERENCES hierarchy_category(hierarchy_category_id) ON UPDATE CASCADE;


--
-- TOC entry 3869 (class 2606 OID 32676)
-- Name: fk_organism_1; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organism
    ADD CONSTRAINT fk_organism_1 FOREIGN KEY (parent_organism_id) REFERENCES organism(organism_id);


--
-- TOC entry 3870 (class 2606 OID 32681)
-- Name: fk_organism_2; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organism
    ADD CONSTRAINT fk_organism_2 FOREIGN KEY (hierarchy_category_id) REFERENCES hierarchy_category(hierarchy_category_id) ON UPDATE CASCADE;


--
-- TOC entry 3873 (class 2606 OID 32686)
-- Name: fk_organism_local_1; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organism_locale
    ADD CONSTRAINT fk_organism_local_1 FOREIGN KEY (organism_id) REFERENCES organism(organism_id);


--
-- TOC entry 3839 (class 2606 OID 32691)
-- Name: forecast_configuration_crop_organism_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY forecast_configuration
    ADD CONSTRAINT forecast_configuration_crop_organism_id_fkey FOREIGN KEY (crop_organism_id) REFERENCES organism(organism_id);


--
-- TOC entry 3840 (class 2606 OID 32696)
-- Name: forecast_configuration_pest_organism_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY forecast_configuration
    ADD CONSTRAINT forecast_configuration_pest_organism_id_fkey FOREIGN KEY (pest_organism_id) REFERENCES organism(organism_id);


--
-- TOC entry 3841 (class 2606 OID 32701)
-- Name: forecast_configuration_point_of_interest_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY forecast_configuration
    ADD CONSTRAINT forecast_configuration_point_of_interest_id_fkey FOREIGN KEY (location_point_of_interest_id) REFERENCES point_of_interest(point_of_interest_id);


--
-- TOC entry 3842 (class 2606 OID 32706)
-- Name: forecast_configuration_vips_logic_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY forecast_configuration
    ADD CONSTRAINT forecast_configuration_vips_logic_user_id_fk FOREIGN KEY (vips_logic_user_id) REFERENCES vips_logic_user(user_id);


--
-- TOC entry 3843 (class 2606 OID 32711)
-- Name: forecast_configuration_weather_station_point_of_interest_i_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY forecast_configuration
    ADD CONSTRAINT forecast_configuration_weather_station_point_of_interest_i_fkey FOREIGN KEY (weather_station_point_of_interest_id) REFERENCES point_of_interest(point_of_interest_id);


--
-- TOC entry 3844 (class 2606 OID 32716)
-- Name: forecast_model_configuration_forecast_configuration_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY forecast_model_configuration
    ADD CONSTRAINT forecast_model_configuration_forecast_configuration_id_fkey FOREIGN KEY (forecast_configuration_id) REFERENCES forecast_configuration(forecast_configuration_id);


--
-- TOC entry 3845 (class 2606 OID 32721)
-- Name: forecast_result_forecast_configuration_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY forecast_result
    ADD CONSTRAINT forecast_result_forecast_configuration_id_fkey FOREIGN KEY (forecast_configuration_id) REFERENCES forecast_configuration(forecast_configuration_id);


--
-- TOC entry 3846 (class 2606 OID 32726)
-- Name: forecast_summary_forecast_configuration_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY forecast_summary
    ADD CONSTRAINT forecast_summary_forecast_configuration_id_fkey FOREIGN KEY (forecast_configuration_id) REFERENCES forecast_configuration(forecast_configuration_id);


--
-- TOC entry 3847 (class 2606 OID 32731)
-- Name: gis_observation_gis_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY gis_observation
    ADD CONSTRAINT gis_observation_gis_id_fkey FOREIGN KEY (gis_id) REFERENCES gis(gis_id) ON DELETE CASCADE;


--
-- TOC entry 3851 (class 2606 OID 32736)
-- Name: message_illustration_caption_locale_message_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY message_illustration_caption_locale
    ADD CONSTRAINT message_illustration_caption_locale_message_id_fkey FOREIGN KEY (message_id) REFERENCES message(message_id);


--
-- TOC entry 3852 (class 2606 OID 32741)
-- Name: message_illustration_caption_locale_message_illustration_fk; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY message_illustration_caption_locale
    ADD CONSTRAINT message_illustration_caption_locale_message_illustration_fk FOREIGN KEY (message_id, file_name) REFERENCES message_illustration(message_id, file_name);


--
-- TOC entry 3850 (class 2606 OID 32746)
-- Name: message_illustration_message_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY message_illustration
    ADD CONSTRAINT message_illustration_message_id_fkey FOREIGN KEY (message_id) REFERENCES message(message_id);


--
-- TOC entry 3853 (class 2606 OID 32751)
-- Name: message_locale_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY message_locale
    ADD CONSTRAINT message_locale_created_by_fkey FOREIGN KEY (created_by) REFERENCES vips_logic_user(user_id);


--
-- TOC entry 3854 (class 2606 OID 32756)
-- Name: message_locale_message_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY message_locale
    ADD CONSTRAINT message_locale_message_id_fkey FOREIGN KEY (message_id) REFERENCES message(message_id);


--
-- TOC entry 3855 (class 2606 OID 32761)
-- Name: message_message_tag_message_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY message_message_tag
    ADD CONSTRAINT message_message_tag_message_id_fkey FOREIGN KEY (message_id) REFERENCES message(message_id);


--
-- TOC entry 3856 (class 2606 OID 32766)
-- Name: message_message_tag_message_tag_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY message_message_tag
    ADD CONSTRAINT message_message_tag_message_tag_id_fkey FOREIGN KEY (message_tag_id) REFERENCES message_tag(message_tag_id);


--
-- TOC entry 3849 (class 2606 OID 32771)
-- Name: message_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY message
    ADD CONSTRAINT message_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES organization(organization_id);


--
-- TOC entry 3857 (class 2606 OID 32776)
-- Name: message_tag_locale_message_tag_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY message_tag_locale
    ADD CONSTRAINT message_tag_locale_message_tag_id_fkey FOREIGN KEY (message_tag_id) REFERENCES message_tag(message_tag_id);


--
-- TOC entry 3858 (class 2606 OID 32781)
-- Name: observation_crop_organism_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation
    ADD CONSTRAINT observation_crop_organism_id_fkey FOREIGN KEY (crop_organism_id) REFERENCES organism(organism_id);


--
-- TOC entry 3866 (class 2606 OID 32786)
-- Name: observation_data_schema_organism_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation_data_schema
    ADD CONSTRAINT observation_data_schema_organism_id_fkey FOREIGN KEY (organism_id) REFERENCES organism(organism_id);


--
-- TOC entry 3867 (class 2606 OID 32791)
-- Name: observation_data_schema_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation_data_schema
    ADD CONSTRAINT observation_data_schema_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES organization(organization_id);


--
-- TOC entry 3901 (class 2606 OID 106226)
-- Name: observation_form_shortcut_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation_form_shortcut
    ADD CONSTRAINT observation_form_shortcut_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES organization(organization_id) ON UPDATE CASCADE;


--
-- TOC entry 3865 (class 2606 OID 221265)
-- Name: observation_last_edited_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation
    ADD CONSTRAINT observation_last_edited_by_fkey FOREIGN KEY (last_edited_by) REFERENCES vips_logic_user(user_id);


--
-- TOC entry 3859 (class 2606 OID 32796)
-- Name: observation_location_point_of_interest_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation
    ADD CONSTRAINT observation_location_point_of_interest_id_fkey FOREIGN KEY (location_point_of_interest_id) REFERENCES point_of_interest(point_of_interest_id) ON UPDATE CASCADE;


--
-- TOC entry 3860 (class 2606 OID 32801)
-- Name: observation_observation_method_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation
    ADD CONSTRAINT observation_observation_method_id_fkey FOREIGN KEY (observation_method_id) REFERENCES observation_method(observation_method_id);


--
-- TOC entry 3861 (class 2606 OID 32806)
-- Name: observation_organism_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation
    ADD CONSTRAINT observation_organism_id_fkey FOREIGN KEY (organism_id) REFERENCES organism(organism_id);


--
-- TOC entry 3862 (class 2606 OID 32811)
-- Name: observation_status_changed_by_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation
    ADD CONSTRAINT observation_status_changed_by_user_id_fkey FOREIGN KEY (status_changed_by_user_id) REFERENCES vips_logic_user(user_id);


--
-- TOC entry 3863 (class 2606 OID 32816)
-- Name: observation_status_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation
    ADD CONSTRAINT observation_status_type_id_fkey FOREIGN KEY (status_type_id) REFERENCES observation_status_type(status_type_id);


--
-- TOC entry 3864 (class 2606 OID 32821)
-- Name: observation_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation
    ADD CONSTRAINT observation_user_id_fkey FOREIGN KEY (user_id) REFERENCES vips_logic_user(user_id);


--
-- TOC entry 3868 (class 2606 OID 32826)
-- Name: obsevation_illustration_observation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY observation_illustration
    ADD CONSTRAINT obsevation_illustration_observation_id_fkey FOREIGN KEY (observation_id) REFERENCES observation(observation_id);


--
-- TOC entry 3871 (class 2606 OID 32831)
-- Name: organism_external_resource_fkey_1; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organism_external_resource
    ADD CONSTRAINT organism_external_resource_fkey_1 FOREIGN KEY (external_resource_id) REFERENCES external_resource(external_resource_id);


--
-- TOC entry 3872 (class 2606 OID 32836)
-- Name: organism_external_resource_fkey_2; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organism_external_resource
    ADD CONSTRAINT organism_external_resource_fkey_2 FOREIGN KEY (organism_id) REFERENCES organism(organism_id);


--
-- TOC entry 3874 (class 2606 OID 32841)
-- Name: organization_country_code_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization
    ADD CONSTRAINT organization_country_code_fkey FOREIGN KEY (country_code) REFERENCES country(country_code);


--
-- TOC entry 3903 (class 2606 OID 121363)
-- Name: organization_group_observation_observation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization_group_observation
    ADD CONSTRAINT organization_group_observation_observation_id_fkey FOREIGN KEY (observation_id) REFERENCES observation(observation_id);


--
-- TOC entry 3902 (class 2606 OID 121358)
-- Name: organization_group_observation_organization_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization_group_observation
    ADD CONSTRAINT organization_group_observation_organization_group_id_fkey FOREIGN KEY (organization_group_id) REFERENCES organization_group(organization_group_id);


--
-- TOC entry 3876 (class 2606 OID 32846)
-- Name: organization_group_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization_group
    ADD CONSTRAINT organization_group_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES organization(organization_id);


--
-- TOC entry 3877 (class 2606 OID 32851)
-- Name: organization_group_poi_organization_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization_group_poi
    ADD CONSTRAINT organization_group_poi_organization_group_id_fkey FOREIGN KEY (organization_group_id) REFERENCES organization_group(organization_group_id);


--
-- TOC entry 3878 (class 2606 OID 32856)
-- Name: organization_group_poi_point_of_interest_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization_group_poi
    ADD CONSTRAINT organization_group_poi_point_of_interest_id_fkey FOREIGN KEY (point_of_interest_id) REFERENCES point_of_interest(point_of_interest_id);


--
-- TOC entry 3879 (class 2606 OID 32861)
-- Name: organization_group_user_organization_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization_group_user
    ADD CONSTRAINT organization_group_user_organization_group_id_fkey FOREIGN KEY (organization_group_id) REFERENCES organization_group(organization_group_id);


--
-- TOC entry 3880 (class 2606 OID 32866)
-- Name: organization_group_user_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization_group_user
    ADD CONSTRAINT organization_group_user_user_id_fkey FOREIGN KEY (user_id) REFERENCES vips_logic_user(user_id);


--
-- TOC entry 3881 (class 2606 OID 32871)
-- Name: organization_map_layer_map_layer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization_map_layer
    ADD CONSTRAINT organization_map_layer_map_layer_id_fkey FOREIGN KEY (map_layer_id) REFERENCES map_layer(map_layer_id);


--
-- TOC entry 3882 (class 2606 OID 32876)
-- Name: organization_map_layer_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization_map_layer
    ADD CONSTRAINT organization_map_layer_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES organization(organization_id);


--
-- TOC entry 3875 (class 2606 OID 32881)
-- Name: organization_parent_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY organization
    ADD CONSTRAINT organization_parent_organization_id_fkey FOREIGN KEY (parent_organization_id) REFERENCES organization(organization_id);


--
-- TOC entry 3883 (class 2606 OID 32886)
-- Name: point_of_interest_country_code_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY point_of_interest
    ADD CONSTRAINT point_of_interest_country_code_fkey FOREIGN KEY (country_code) REFERENCES country(country_code);


--
-- TOC entry 3887 (class 2606 OID 32891)
-- Name: point_of_interest_external_resource_external_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY point_of_interest_external_resource
    ADD CONSTRAINT point_of_interest_external_resource_external_resource_id_fkey FOREIGN KEY (external_resource_id) REFERENCES external_resource(external_resource_id);


--
-- TOC entry 3888 (class 2606 OID 32896)
-- Name: point_of_interest_external_resource_point_of_interest_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY point_of_interest_external_resource
    ADD CONSTRAINT point_of_interest_external_resource_point_of_interest_id_fkey FOREIGN KEY (point_of_interest_id) REFERENCES point_of_interest(point_of_interest_id);


--
-- TOC entry 3884 (class 2606 OID 32901)
-- Name: point_of_interest_point_of_interest_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY point_of_interest
    ADD CONSTRAINT point_of_interest_point_of_interest_type_id_fkey FOREIGN KEY (point_of_interest_type_id) REFERENCES point_of_interest_type(point_of_interest_type_id);


--
-- TOC entry 3885 (class 2606 OID 32906)
-- Name: point_of_interest_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY point_of_interest
    ADD CONSTRAINT point_of_interest_user_id_fkey FOREIGN KEY (user_id) REFERENCES vips_logic_user(user_id);


--
-- TOC entry 3886 (class 2606 OID 32911)
-- Name: point_of_interest_weather_forecast_provider_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY point_of_interest
    ADD CONSTRAINT point_of_interest_weather_forecast_provider_id_fkey FOREIGN KEY (weather_forecast_provider_id) REFERENCES weather_forecast_provider(weather_forecast_provider_id);


--
-- TOC entry 3889 (class 2606 OID 32916)
-- Name: point_of_interest_weather_station_fk1; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY point_of_interest_weather_station
    ADD CONSTRAINT point_of_interest_weather_station_fk1 FOREIGN KEY (point_of_interest_id) REFERENCES point_of_interest(point_of_interest_id);


--
-- TOC entry 3890 (class 2606 OID 32921)
-- Name: point_of_interest_weather_station_fk2; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY point_of_interest_weather_station
    ADD CONSTRAINT point_of_interest_weather_station_fk2 FOREIGN KEY (weather_station_data_source_id) REFERENCES weather_station_data_source(weather_station_data_source_id);


--
-- TOC entry 3891 (class 2606 OID 32926)
-- Name: preparation_currency_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY preparation
    ADD CONSTRAINT preparation_currency_id_fkey FOREIGN KEY (currency_id) REFERENCES currency(currency_id);


--
-- TOC entry 3892 (class 2606 OID 32931)
-- Name: preparation_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY preparation
    ADD CONSTRAINT preparation_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES organization(organization_id);


--
-- TOC entry 3893 (class 2606 OID 32936)
-- Name: preparation_preparation_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY preparation
    ADD CONSTRAINT preparation_preparation_type_id_fkey FOREIGN KEY (preparation_type_id) REFERENCES preparation_type(preparation_type_id);


--
-- TOC entry 3894 (class 2606 OID 32941)
-- Name: task_history_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY task_history
    ADD CONSTRAINT task_history_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES organization(organization_id);


--
-- TOC entry 3895 (class 2606 OID 32946)
-- Name: task_history_task_history_status_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY task_history
    ADD CONSTRAINT task_history_task_history_status_id_fkey FOREIGN KEY (task_history_status_id) REFERENCES task_history_status(task_history_status_id);


--
-- TOC entry 3896 (class 2606 OID 32951)
-- Name: user_authentication_user_authentication_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY user_authentication
    ADD CONSTRAINT user_authentication_user_authentication_type_id_fkey FOREIGN KEY (user_authentication_type_id) REFERENCES user_authentication_type(user_authentication_type_id);


--
-- TOC entry 3897 (class 2606 OID 32956)
-- Name: user_authentication_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY user_authentication
    ADD CONSTRAINT user_authentication_user_id_fkey FOREIGN KEY (user_id) REFERENCES vips_logic_user(user_id);


--
-- TOC entry 3898 (class 2606 OID 32961)
-- Name: user_uuid_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY user_uuid
    ADD CONSTRAINT user_uuid_user_id_fkey FOREIGN KEY (user_id) REFERENCES vips_logic_user(user_id);


--
-- TOC entry 3899 (class 2606 OID 32966)
-- Name: user_vips_logic_role_fk; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY user_vips_logic_role
    ADD CONSTRAINT user_vips_logic_role_fk FOREIGN KEY (vips_logic_role_id) REFERENCES vips_logic_role(vips_logic_role_id);


--
-- TOC entry 3900 (class 2606 OID 32971)
-- Name: vips_logic_user_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vipslogic
--

ALTER TABLE ONLY vips_logic_user
    ADD CONSTRAINT vips_logic_user_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES organization(organization_id);


--
-- TOC entry 4114 (class 0 OID 0)
-- Dependencies: 11
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2017-10-09 16:32:09 CEST

--
-- PostgreSQL database dump complete
--


---------- #################################### DATA INSERTS!!!!
-- Data that are needed in order to initalize the database

-- public.country
INSERT INTO country (country_code, phone_code) VALUES ('AD', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('AE', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('AF', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('AG', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('AI', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('AL', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('AM', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('AN', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('AO', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('AQ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('AR', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('AS', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('AT', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('AU', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('AW', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('AX', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('AZ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BB', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BD', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BE', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BF', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BG', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BH', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BI', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BJ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BL', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BM', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BN', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BO', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BQ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BR', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BS', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BT', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BV', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BW', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BY', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('BZ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CA', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CC', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CD', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CF', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CG', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CH', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CI', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CK', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CL', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CM', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CN', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CO', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CR', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CU', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CV', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CW', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CX', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CY', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('CZ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('DE', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('DJ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('DK', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('DM', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('DO', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('DZ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('EC', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('EE', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('EG', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('EH', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('ER', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('ES', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('ET', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('FI', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('FJ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('FK', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('FM', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('FO', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('FR', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GA', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GB', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GD', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GE', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GF', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GG', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GH', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GI', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GL', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GM', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GN', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GP', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GQ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GR', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GS', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GT', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GU', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GW', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('GY', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('HK', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('HM', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('HN', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('HR', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('HT', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('HU', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('ID', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('IE', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('IL', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('IM', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('IN', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('IO', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('IQ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('IR', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('IS', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('IT', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('JE', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('JM', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('JO', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('JP', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('KE', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('KG', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('KH', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('KI', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('KM', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('KN', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('KP', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('KR', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('KW', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('KY', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('KZ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('LA', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('LB', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('LC', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('LI', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('LK', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('LR', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('LS', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('LT', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('LU', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('LY', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MA', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MC', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MD', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('ME', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MF', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MG', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MH', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MK', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('ML', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MM', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MN', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MO', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MP', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MQ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MR', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MS', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MT', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MU', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MV', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MW', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MX', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MY', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('MZ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('NA', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('NC', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('NE', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('NF', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('NG', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('NI', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('NL', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('NP', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('NR', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('NU', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('NZ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('OM', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('PA', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('PE', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('PF', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('PG', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('PH', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('PK', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('PL', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('PM', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('PN', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('PR', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('PS', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('PT', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('PW', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('PY', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('QA', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('RE', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('RO', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('RS', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('RU', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('RW', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('SA', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('SB', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('SC', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('SD', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('SG', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('SH', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('SI', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('SJ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('SK', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('SL', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('SM', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('SN', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('SO', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('SR', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('ST', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('SV', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('SX', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('SY', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('SZ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('TC', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('TD', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('TF', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('TG', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('TH', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('TJ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('TK', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('TL', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('TM', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('TN', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('TO', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('TR', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('TT', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('LV', '371');
INSERT INTO country (country_code, phone_code) VALUES ('TV', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('TW', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('TZ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('UA', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('UG', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('UM', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('US', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('UY', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('UZ', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('VA', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('VC', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('VE', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('VG', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('VI', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('VN', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('VU', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('WF', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('WS', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('YE', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('YT', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('ZA', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('ZM', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('ZW', NULL);
INSERT INTO country (country_code, phone_code) VALUES ('NO', '47');
INSERT INTO country (country_code, phone_code) VALUES ('SE', '46');
INSERT INTO country (country_code, phone_code) VALUES ('BA', '387');


-- public.external_resource_type
INSERT INTO external_resource_type (external_resource_type_id, default_name) VALUES (2, 'Organism database');
INSERT INTO external_resource_type (external_resource_type_id, default_name) VALUES (3, 'Pest/disease forecast provider');

-- public.preparation_type
INSERT INTO preparation_type (preparation_type_id,preparation_type_name)
VALUES (1, 'Fungicide');
INSERT INTO preparation_type (preparation_type_id,preparation_type_name)
VALUES (2, 'Insecticide');
INSERT INTO preparation_type (preparation_type_id,preparation_type_name)
VALUES (3, 'Herbicide');
INSERT INTO preparation_type (preparation_type_id,preparation_type_name)
VALUES (4, 'Fungicide/insecticide');
INSERT INTO preparation_type (preparation_type_id,preparation_type_name)
VALUES (5, 'Fungicide/bactericide');
INSERT INTO preparation_type (preparation_type_id,preparation_type_name)
VALUES (6, 'Insecticide/molluscicide');
INSERT INTO preparation_type (preparation_type_id,preparation_type_name)
VALUES (7, 'Herbicide/insecticide');

-- public.currency
INSERT INTO public.currency(currency_id, currency_name) VALUES('NOK','Norwegian Krone');
INSERT INTO public.currency(currency_id, currency_name) VALUES('SEK','Swedish Krone');
INSERT INTO public.currency(currency_id, currency_name) VALUES('EUR','Euro');
INSERT INTO public.currency(currency_id, currency_name) VALUES('BAM','Bosnian Convertible Mark');

-- messaging.universal_message_format
INSERT INTO messaging.universal_message_format(universal_message_format_id,format_name)VALUES(1,'Mail');
INSERT INTO messaging.universal_message_format(universal_message_format_id,format_name)VALUES(2,'Sms');

-- public.messaging.forecast_event
INSERT INTO messaging.forecast_event(forecast_event_id, event_name, event_description) VALUES (1,'TO_RED','Forecast changed to high infection risk');
INSERT INTO messaging.forecast_event(forecast_event_id, event_name, event_description) VALUES (2,'GREEN_TO_YELLOW','Forecast changed from no risk to potential infection risk');

-- public.observation_status
INSERT INTO public.observation_status_type(status_type_id, status_title) VALUES(1,'Pending');
INSERT INTO public.observation_status_type(status_type_id, status_title) VALUES(2,'Rejected');
INSERT INTO public.observation_status_type(status_type_id, status_title) VALUES(3,'Approved');

-- public.organism
INSERT INTO public.organism(organism_id,trade_name) VALUES(-10, 'Missing in database'); 

-- public.point_of_interest_type
INSERT INTO public.point_of_interest_type(point_of_interest_type_id, default_name) VALUES(0,'General');

-- public.hierarchy_category
INSERT INTO hierarchy_category (hierarchy_category_id, default_name, logically_deleted) VALUES (10, 'Kingdom', false);
INSERT INTO hierarchy_category (hierarchy_category_id, default_name, logically_deleted) VALUES (20, 'Sub-kingdom', false);
INSERT INTO hierarchy_category (hierarchy_category_id, default_name, logically_deleted) VALUES (30, 'Phylum', false);
INSERT INTO hierarchy_category (hierarchy_category_id, default_name, logically_deleted) VALUES (40, 'Sub-Phylum', false);
INSERT INTO hierarchy_category (hierarchy_category_id, default_name, logically_deleted) VALUES (50, 'Class', false);
INSERT INTO hierarchy_category (hierarchy_category_id, default_name, logically_deleted) VALUES (60, 'Sub-Class', false);
INSERT INTO hierarchy_category (hierarchy_category_id, default_name, logically_deleted) VALUES (70, 'Super-Order', false);
INSERT INTO hierarchy_category (hierarchy_category_id, default_name, logically_deleted) VALUES (80, 'Order', false);
INSERT INTO hierarchy_category (hierarchy_category_id, default_name, logically_deleted) VALUES (90, 'Sub-Order', false);
INSERT INTO hierarchy_category (hierarchy_category_id, default_name, logically_deleted) VALUES (100, 'Super-family', false);
INSERT INTO hierarchy_category (hierarchy_category_id, default_name, logically_deleted) VALUES (110, 'Family', false);
INSERT INTO hierarchy_category (hierarchy_category_id, default_name, logically_deleted) VALUES (115, 'Genus', false);
INSERT INTO hierarchy_category (hierarchy_category_id, default_name, logically_deleted) VALUES (120, 'Species', false);
INSERT INTO hierarchy_category (hierarchy_category_id, default_name, logically_deleted) VALUES (1000, 'Unknown', false);
INSERT INTO hierarchy_category (hierarchy_category_id, default_name, logically_deleted) VALUES (123, 'Division', false);
INSERT INTO hierarchy_category (hierarchy_category_id, default_name, logically_deleted) VALUES (125, 'Type', false);
INSERT INTO hierarchy_category (hierarchy_category_id, default_name, logically_deleted) VALUES (112, 'Sub-family', false);
INSERT INTO hierarchy_category (hierarchy_category_id, default_name, logically_deleted) VALUES (121, 'Sub-Species', false);

-- public.hierarchy_category_locale
INSERT INTO hierarchy_category_locale (hierarchy_category_id, locale, local_name) VALUES (10, 'nb', 'Rike');
INSERT INTO hierarchy_category_locale (hierarchy_category_id, locale, local_name) VALUES (20, 'nb', 'Underrike');
INSERT INTO hierarchy_category_locale (hierarchy_category_id, locale, local_name) VALUES (30, 'nb', 'Rekke');
INSERT INTO hierarchy_category_locale (hierarchy_category_id, locale, local_name) VALUES (40, 'nb', 'Underrekke');
INSERT INTO hierarchy_category_locale (hierarchy_category_id, locale, local_name) VALUES (50, 'nb', 'Klasse');
INSERT INTO hierarchy_category_locale (hierarchy_category_id, locale, local_name) VALUES (60, 'nb', 'Underklasse');
INSERT INTO hierarchy_category_locale (hierarchy_category_id, locale, local_name) VALUES (70, 'nb', 'Overorden');
INSERT INTO hierarchy_category_locale (hierarchy_category_id, locale, local_name) VALUES (80, 'nb', 'Orden');
INSERT INTO hierarchy_category_locale (hierarchy_category_id, locale, local_name) VALUES (90, 'nb', 'Underorden');
INSERT INTO hierarchy_category_locale (hierarchy_category_id, locale, local_name) VALUES (100, 'nb', 'Overfamilie');
INSERT INTO hierarchy_category_locale (hierarchy_category_id, locale, local_name) VALUES (110, 'nb', 'Familie');
INSERT INTO hierarchy_category_locale (hierarchy_category_id, locale, local_name) VALUES (112, 'nb', 'Underfamilie');
INSERT INTO hierarchy_category_locale (hierarchy_category_id, locale, local_name) VALUES (115, 'nb', 'Slekt');
INSERT INTO hierarchy_category_locale (hierarchy_category_id, locale, local_name) VALUES (120, 'nb', 'Art');
INSERT INTO hierarchy_category_locale (hierarchy_category_id, locale, local_name) VALUES (123, 'nb', 'Divisjon');
INSERT INTO hierarchy_category_locale (hierarchy_category_id, locale, local_name) VALUES (125, 'nb', 'Sort');
INSERT INTO hierarchy_category_locale (hierarchy_category_id, locale, local_name) VALUES (1000, 'nb', 'Ukjent');
INSERT INTO hierarchy_category_locale (hierarchy_category_id, locale, local_name) VALUES (121, 'nb', 'Underart');

-- public.message_tag
INSERT INTO message_tag (message_tag_id, default_tag_name) VALUES (2, 'Advisory message');
INSERT INTO message_tag (message_tag_id, default_tag_name) VALUES (3, 'System status');
INSERT INTO message_tag (message_tag_id, default_tag_name) VALUES (4, 'Apple fruit moth');

-- public.message_tag_locale
INSERT INTO message_tag_locale (message_tag_id, locale, local_name) VALUES (2, 'nb', 'Fagmelding');
INSERT INTO message_tag_locale (message_tag_id, locale, local_name) VALUES (3, 'nb', 'Driftsmelding');
INSERT INTO message_tag_locale (message_tag_id, locale, local_name) VALUES (2, 'bs', 'Preporuke savjetodavca');
INSERT INTO message_tag_locale (message_tag_id, locale, local_name) VALUES (3, 'bs', 'Smetnje');
INSERT INTO message_tag_locale (message_tag_id, locale, local_name) VALUES (4, 'nb', 'Rognebærmøll');

-- public.observation_method
INSERT INTO observation_method (observation_method_id) VALUES ('NOT_REGISTERED');
INSERT INTO observation_method (observation_method_id) VALUES ('KNOCKING');
INSERT INTO observation_method (observation_method_id) VALUES ('HARVESTING');
INSERT INTO observation_method (observation_method_id) VALUES ('COUNTING');
INSERT INTO observation_method (observation_method_id) VALUES ('VISUAL');

-- public.vips_logic_role
INSERT INTO vips_logic_role (vips_logic_role_id, default_title, default_description) VALUES (1, 'Superuser', 'Controls the whole application');
INSERT INTO vips_logic_role (vips_logic_role_id, default_title, default_description) VALUES (2, 'Organization administrator', 'Controls the all aspects of the application in relation to the user''s organization');
INSERT INTO vips_logic_role (vips_logic_role_id, default_title, default_description) VALUES (3, 'Observer', 'Registers observations of pests and diseases');
INSERT INTO vips_logic_role (vips_logic_role_id, default_title, default_description) VALUES (4, 'Observation authority', 'Approves observations made by others');
INSERT INTO vips_logic_role (vips_logic_role_id, default_title, default_description) VALUES (5, 'Message author', 'Writes messages');
INSERT INTO vips_logic_role (vips_logic_role_id, default_title, default_description) VALUES (6, 'Apple fruit moth administrator', 'Registers stations for and observations of apple fruit moth');
INSERT INTO vips_logic_role (vips_logic_role_id, default_title, default_description) VALUES (7, 'Organism editor', 'Can create, edit, move and delete organisms');

-- public.weather_station_data_source
INSERT INTO weather_station_data_source (weather_station_data_source_id, name, default_description, uri, datafetch_uri_expression, info_uri_expression) VALUES (3, 'Landbruksmeteorologisk tjeneste - GRID', 'GRID data (stored forecasts) for AgroMet Norway weather stations', 'http://lmt.bioforsk.no/', 'http://lmt.bioforsk.no/agrometbase/export/getVIPS3JSONWeatherData.php?forecastsOnly=true&weatherStationId=%s', '');
INSERT INTO weather_station_data_source (weather_station_data_source_id, name, default_description, uri, datafetch_uri_expression, info_uri_expression) VALUES (4, 'Metos proxy', 'Proxy service for data from Metos', 'http://www.fieldclimate.com/', 'http://logic.vips.bioforsk.no/rest/weather/proxy/metos/%s', '');
INSERT INTO weather_station_data_source (weather_station_data_source_id, name, default_description, uri, datafetch_uri_expression, info_uri_expression) VALUES (5, 'Sveriges Landbruksuniversitet', NULL, 'http://www.ffe.slu.se', 'http://www.ffe.slu.se/lm/vips/FFJSON.cfm?weatherStationID=%s', '');
INSERT INTO weather_station_data_source (weather_station_data_source_id, name, default_description, uri, datafetch_uri_expression, info_uri_expression) VALUES (6, 'Finland A-Lab', 'MaaSää network of A-Lab weather stations', 'http://maasaa.a-log.net/mapview.php', 'http://logic.vips.bioforsk.no/rest/weather/proxy/alab/%s?userName=fmi&password=ectoncha', '');
INSERT INTO weather_station_data_source (weather_station_data_source_id, name, default_description, uri, datafetch_uri_expression, info_uri_expression) VALUES (7, 'fruitweb Davis stations', 'Davis stations data collected by German company fruitweb', 'http://www.fruitweb.info/', 'http://logic.vips.nibio.no/rest/weather/proxy/fruitwebdavis/%s', '');
INSERT INTO weather_station_data_source (weather_station_data_source_id, name, default_description, uri, datafetch_uri_expression, info_uri_expression) VALUES (1, 'Landbruksmeteorologisk tjeneste', 'Norwegian agrometorogical service', 'http://lmt.bioforsk.no/', 'http://lmt.bioforsk.no/agrometbase/export/getVIPS3JSONWeatherData.php?weatherStationId=%s', 'http://lmt.nibio.no/weatherstations/%s/chart?log_interval=1');

-- public.task_history_status
INSERT INTO task_history_status (task_history_status_id, default_title) VALUES ('taskHistoryStatusOK', 'OK');
INSERT INTO task_history_status (task_history_status_id, default_title) VALUES ('taskHistoryStatusFailedPartly', 'Failed partly');
INSERT INTO task_history_status (task_history_status_id, default_title) VALUES ('taskHistoryStatusFailedCompletely', 'Failed completely');

-- public.weather_forecast_provider
INSERT INTO weather_forecast_provider (weather_forecast_provider_id, name, info_url, description) VALUES (1, 'YR.no', 'http://www.yr.no/', 'Norwegian free worldwide forecast service');

-- public.user_authentication_type
INSERT INTO user_authentication_type (user_authentication_type_id, name, description) VALUES (1, 'PASSWORD', 'Combination of username and password');
INSERT INTO user_authentication_type (user_authentication_type_id, name, description) VALUES (2, 'OPENID', 'OpenId protocol. See http://openid.net/');
INSERT INTO user_authentication_type (user_authentication_type_id, name, description) VALUES (3, 'OPENID_GOOGLE', 'Google signin with Google''s implementation of OpenId. See https://developers.google.com/accounts/docs/OpenID');

-- public.organization
-- MUST BE CREATED ON STARTUP, along with super user



