
drop table if exists caom2.Plane;

create table caom2.Plane
(
    planeURI varchar(512) not null,
    productID varchar(64) not null,

    metaRelease timestamp,
    dataRelease timestamp,
    dataProductType varchar(64),
    calibrationLevel integer,

-- provenance
    provenance_name varchar(64),
    provenance_reference text,
    provenance_version varchar(64),
    provenance_project varchar(256),
    provenance_producer varchar(256),
    provenance_runID varchar(256),
    provenance_lastExecuted timestamp,
    provenance_keywords text,
    provenance_inputs text,

-- metrics
    metrics_sourceNumberDensity double precision,
    metrics_background double precision,
    metrics_backgroundStddev double precision,
    metrics_fluxDensityLimit double precision,
    metrics_magLimit double precision,

-- position
    position_bounds          spoly,
    position_bounds_center   spoint,
    position_bounds_area     double precision,
    position_bounds_size     double precision,
    position_bounds_encoded  bytea,
    position_dimension1      bigint,
    position_dimension2      bigint,
    position_resolution      double precision,
    position_sampleSize      double precision,
    position_timeDependent   integer,

-- energy
    energy_emband            varchar(32),
    energy_bounds_cval1      double precision,
    energy_bounds_cval2      double precision,
    energy_bounds_width      double precision,
    energy_bounds_integrated double precision,
    energy_dimension         bigint,
    energy_resolvingPower    double precision,
    energy_sampleSize        double precision,
    energy_bandpassName      varchar(64),
    energy_transition_species varchar(32),
    energy_transition_transition varchar(32),
    energy_freqWidth         double precision,
    energy_freqSampleSize    double precision,

-- time
    time_bounds_cval1       double precision,
    time_bounds_cval2       double precision,
    time_bounds_width       double precision,
    time_bounds_integrated  double precision,
    time_dimension          bigint,
    time_resolution         double precision,
    time_sampleSize         double precision,
    time_exposure           double precision,

-- polarization
    polarization_states     varchar(34),
    polarization_dimension  integer,

-- internal
    obsID bigint not null references caom2.Observation (obsID),
    planeID bigint not null  primary key using index tablespace caom_index,
    lastModified timestamp not null,
    maxLastModified timestamp not null,
    stateCode int not null
)
tablespace caom_data
;

-- this is for Observation join Plane
create index i_obsID on caom2.Plane (obsID)
tablespace caom_index
;

-- tag the clustering index
cluster i_obsID on caom2.Plane
;

-- plane.input join support
drop table if exists caom2.Plane_inputs;

create table caom2.Plane_inputs
(
    outputID bigint not null references caom2.Plane (planeID),
    inputID bigint not null references caom2.Plane (planeID)
)
tablespace caom_data
;

create unique index i_output2input on caom2.Plane_inputs (outputID,inputID)
tablespace caom_index
;

create unique index i_input2output on caom2.Plane_inputs (inputID,outputID)
tablespace caom_index
;